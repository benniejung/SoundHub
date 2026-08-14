package com.yebin.sideproject.domain.chatbot.service;

import com.yebin.sideproject.domain.chatbot.entity.ChatMessage;
import com.yebin.sideproject.domain.chatbot.entity.MessageRole;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class ChatService {
    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    // 세션(사용자)별 대화 히스토리. 인메모리라 서버 재시작 시 초기화됨.
    // 지금은 세션 구분 없이 단일 키로 저장 — 나중에 JWT/세션ID가 생기면 DEFAULT_SESSION_ID 대신 그 값을 키로 쓰면 됨.
    private static final String DEFAULT_SESSION_ID = "default";
    private final Map<String, List<ChatMessage>> sessionHistories = new ConcurrentHashMap<>();

    public ChatService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
    }

    private static final String SYSTEM_PROMPT = """
        너는 아티스트의 음원, 응원법, 콘서트 정보를 안내해주는 팬 전용 챗봇이야.

        [말투 규칙]
        - 친근하고 다정한 반말 대신, 팬을 존중하는 정중한 존댓말을 사용해.
        - 딱딱한 설명체보다는 팬 커뮤니티에서 이야기하듯 친근하게 답변해.
        - 이모지는 과하지 않게, 필요한 곳에만 한두 개 정도 사용해.
        - 답변은 너무 길지 않게, 핵심만 간결하게 전달해.

        [답변 규칙]
        - 절대 추측하지 말고 "확인된 정보가 없어요 😢 정확한 정보는 공식 채널을 확인해주세요"라고 답변해.
        - 콘서트 정보(일정, 장소, 티켓)를 안내할 때는 [콘서트 정보]를 참고해서 날짜/장소를 먼저 명확히 말하고, 주의사항이 있다면 마지막에 덧붙여.
        - 곡 정보(가사, 응원법 등)를 안내할 때는 [참고 자료 검색 결과]에 있는 내용을 근거로 답변해.
        
        [콘서트 정보]
        [공연명] 2026 OO 콘서트 <TOUR TITLE>
        [아티스트] OO
        [일시] 2026년 9월 12일(토) ~ 9월 13일(일), 공연 시작 오후 6시 (입장 오후 4시부터)
        [장소] KSPO DOME (서울 송파구 올림픽로 424)
        [좌석 등급 및 가격]
        - VIP석: 220,000원
        - R석: 187,000원
        - S석: 154,000원
        [예매처] 인터파크 티켓 (단독 판매)
        [예매 일정] 팬클럽 선예매 8/20(목) 20:00 / 일반 예매 8/22(토) 20:00
        [유의사항]
        - 1인 최대 2매 예매 가능, 티켓 양도 시 반드시 본인 인증 필요
        - 공연장 내 응원봉 반입 가능, 별도 응원봉 대여는 불가

        [참고 자료 검색 결과]
        %s
        """;

    public String createChat(String msg) {

        List<ChatMessage> history = sessionHistories.computeIfAbsent(DEFAULT_SESSION_ID, id -> new CopyOnWriteArrayList<>());

        // 벡터 DB에서 질문과 유사한 참고자료 검색
        List<Document> searchResults = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(msg)
                        .similarityThreshold(0.75) // 관련성 낮은 건 참고 방지
                        .build()
        );

        // 검색 결과가 없으면 굳이 AI 호출 안 하고 바로 응답
        if (searchResults.isEmpty()) {
            return "확인된 정보가 없어요 😢 정확한 정보는 공식 채널을 확인해주세요.";
        }

        String context = searchResults.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n---\n"));

        // 지난 대화 히스토리를 Spring AI 메시지 타입으로 변환
        List<Message> previousMessages = history.stream()
                .map(this::toSpringAiMessage)
                .toList();

        // OpenAI 요청 옵션 설정 (완성된 옵션이 아니라 builder를 그대로 넘겨야 함)
        OpenAiChatOptions.Builder chatOptions = OpenAiChatOptions.builder()
                .model("gpt-4o")
                .maxTokens(500)
                .temperature(0.5); // 창의성, 무작위성 정도 조절(0.5가 적당히 다양한 근거와 논리를 가지고 답변하는 정도)

        // 참고자료 + 히스토리를 함께 넣어 AI 응답 생성 요청
        String answer = chatClient
                .prompt()
                .system(SYSTEM_PROMPT.formatted(context))
                .messages(previousMessages)
                .user(msg)
                .options(chatOptions)
                .call() // AI 호출
                .content(); // 응답 텍스트 추출

        history.add(ChatMessage.of(MessageRole.USER, msg));
        history.add(ChatMessage.of(MessageRole.ASSISTANT, answer));

        return answer;
    }

    private Message toSpringAiMessage(ChatMessage message) {
        return message.role() == MessageRole.USER
                ? new UserMessage(message.content())
                : new AssistantMessage(message.content());
    }
}
