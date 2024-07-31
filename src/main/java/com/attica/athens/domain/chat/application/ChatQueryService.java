package com.attica.athens.domain.chat.application;

import com.attica.athens.domain.agora.dao.AgoraRepository;
import com.attica.athens.domain.agora.exception.NotFoundAgoraException;
import com.attica.athens.domain.agoraMember.dao.AgoraMemberRepository;
import com.attica.athens.domain.agoraMember.domain.AgoraMember;
import com.attica.athens.domain.agoraMember.domain.AgoraMemberType;
import com.attica.athens.domain.chat.dao.ChatRepository;
import com.attica.athens.domain.chat.domain.Chat;
import com.attica.athens.domain.chat.domain.Chats;
import com.attica.athens.domain.chat.dto.Cursor;
import com.attica.athens.domain.chat.dto.response.GetChatParticipants;
import com.attica.athens.domain.chat.dto.response.GetChatResponse;
import com.attica.athens.domain.chat.dto.response.GetChatResponse.ChatData;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatQueryService {

    private final AgoraRepository agoraRepository;
    private final AgoraMemberRepository agoraMemberRepository;
    private final ChatRepository chatRepository;

    public GetChatResponse getChatHistory(final Long agoraId, final Cursor cursor) {
        validateAgoraExists(agoraId);

        List<AgoraMember> agoraMembers = findAgoraMembers(agoraId);
        Chats chats = new Chats(findChats(cursor, agoraMembers));
        List<ChatData> chatData = chats.createChatDataWithUsers(agoraMembers);
        Cursor nextCursor = cursor.calculateNext(chats);

        return new GetChatResponse(chatData, nextCursor);
    }

    public GetChatParticipants getChatParticipants(final Long agoraId) {
        validateAgoraExists(agoraId);

        return new GetChatParticipants(findActiveParticipants(agoraId), agoraId);
    }

    private void validateAgoraExists(final Long agoraId) {
        agoraRepository.findById(agoraId)
                .orElseThrow(() -> new NotFoundAgoraException(agoraId));
    }

    private List<AgoraMember> findAgoraMembers(final Long agoraId) {
        return agoraMemberRepository.findByAgoraId(agoraId);
    }

    private List<Chat> findChats(final Cursor cursor, final List<AgoraMember> agoraMembers) {
        List<Long> agoraMemberIds = agoraMembers.stream()
                .map(AgoraMember::getId)
                .toList();
        Pageable pageable = PageRequest.of(0, cursor.getEffectiveSize());

        return chatRepository.findChatsForAgoraMembers(agoraMemberIds,
                cursor.hasKey() ? cursor.key() : null,
                pageable);
    }

    private List<AgoraMember> findActiveParticipants(final Long agoraId) {
        return agoraMemberRepository.findByAgoraIdAndTypeInAndSessionIdIsNotNull(agoraId,
                List.of(AgoraMemberType.PROS, AgoraMemberType.CONS));
    }
}
