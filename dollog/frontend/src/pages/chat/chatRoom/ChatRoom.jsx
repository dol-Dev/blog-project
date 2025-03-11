import { Client } from '@stomp/stompjs';
import React, { useEffect, useRef, useState } from 'react';
import Draggable from 'react-draggable';
import { toast } from 'react-toastify';
import 'semantic-ui-css/semantic.min.css';
import { Button, Divider, Header, Icon, Image, List } from 'semantic-ui-react';
import SockJS from 'sockjs-client';
import { useAuth } from '../../../contexts/AuthContext';
import AVATAR_URL from '../../../utils/avatarUrl';
import axiosInstance from '../../../utils/axiosInstance';
import BASE_URL from '../../../utils/baseUrl';
import Chat from '../chat/Chat';
import styles from './chatRoom.module.css';

const ChatRoom = () => {
    const [visible, setVisible] = useState(false);
    const [buttonVisible, setButtonVisible] = useState(true);
    const [chatPosition, setChatPosition] = useState({ x: 0, y: 0 });
    const [scrollPosition, setScrollPosition] = useState(0);
    const [dragging, setDragging] = useState(false);
    const [rooms, setRooms] = useState([]);
    const [selectedRoom, setSelectedRoom] = useState(null);
    const clientRef = useRef(null);
    const { authInfo } = useAuth();
    const [myNickname, setMyNickname] = useState('');
    const [myAvatarImageName, setMyAvatarImageName] = useState('');

    // 인증 정보 설정 및 방 목록 가져오기
    useEffect(() => {
        if (authInfo) {
            setMyNickname(authInfo.nickname);
            setMyAvatarImageName(authInfo.avatarImageName);
        }
        fetchRooms();
    }, [authInfo]);

    // STOMP 연결 설정
    useEffect(() => {
        const socket = new SockJS(BASE_URL + '/ws', null, { withCredentials: true });
        const client = new Client({
            webSocketFactory: () => socket,
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
            onStompError: (frame) => {
                console.error("STOMP error", frame);
            }
        });
        client.activate();
        clientRef.current = client;
        return () => {
            client.deactivate();
        };
    }, []);

    // 채팅방 목록 가져오기
    const fetchRooms = async () => {
        try {
            const response = await axiosInstance.get(`/api/chatRooms/myRooms`);
            if (response.status === 200) {
                const activeRooms = response.data.data.filter(room => room.participantStatus === "ACTIVE");
                setRooms(activeRooms);
            }
        } catch (error) {
            console.error('Fail to fetch rooms:', error);
        }
    };

    // 방 나가기
    const leaveRoom = async (roomId) => {
        try {
            const response = await axiosInstance.delete(`/api/chatRooms/${roomId}/leave`, {
                params: { participant: myNickname }
            });
            if (response.status === 200) {
                fetchRooms();
            }
        } catch (error) {
            toast.error('나가기 실패. 다시 시도해주세요');
        }
    };

    // 채팅방 선택
    const handleSelectRoom = (room) => {
        setSelectedRoom(room);
        setVisible(false);
        setButtonVisible(false);
    };

    // 스크롤 및 키 이벤트 처리
    useEffect(() => {
        const handleScroll = () => {
            setScrollPosition(window.scrollY * 1.17);
        };
        const handleKeyDown = (event) => {
            if (event.key === 'Escape') {
                if (selectedRoom) {
                    setSelectedRoom(null);
                } else if (visible) {
                    setVisible(false);
                    setButtonVisible(true);
                }
            }
        };
        window.addEventListener('scroll', handleScroll);
        window.addEventListener('keydown', handleKeyDown);
        return () => {
            window.removeEventListener('scroll', handleScroll);
            window.removeEventListener('keydown', handleKeyDown);
        };
    }, [selectedRoom, visible]);

    // 채팅창 위치 업데이트
    useEffect(() => {
        if (visible) {
            setChatPosition(prev => ({ ...prev, y: scrollPosition }));
        }
    }, [scrollPosition, visible]);

    // 드래그 관련 함수
    const handleDrag = (e, data) => {
        if (dragging) {
            setChatPosition(prev => ({
                x: prev.x + data.deltaX,
                y: prev.y + data.deltaY,
            }));
        }
    };
    const handleStop = (e, data) => {
        setChatPosition({ x: data.x, y: data.y });
        setDragging(false);
    };
    const handleStart = () => { setDragging(true); };

    // 채팅창 토글
    const toggleChat = () => {
        setVisible(!visible);
        setButtonVisible(false);
    };

    return (
        <div>
            {buttonVisible && (
                <Button
                    className={styles.chatButton}
                    style={{ transform: `translateY(${scrollPosition}px)` }}
                    size="huge"
                    icon
                    onClick={toggleChat}
                >
                    <Icon name="chat" />
                </Button>
            )}

            {visible && !selectedRoom && (
                <Draggable
                    position={chatPosition}
                    onStart={handleStart}
                    onDrag={handleDrag}
                    onStop={handleStop}
                >
                    <div className={styles.chatRoomContainer}>
                        <div className={styles.chatRoomHeader}>
                            <Header as="h3">1:1 채팅</Header>
                            <Button
                                className={styles.chatRoomCloseButton}
                                onClick={() => { setVisible(false); setButtonVisible(true); }}
                                icon
                            >
                                <Icon name="x" />
                            </Button>
                        </div>
                        <Divider className={styles.customDivider} />

                        {/* 중복 제거 후 방 목록 렌더링 */}
                        {(() => {
                            const uniqueRooms = new Map();
                            rooms.forEach(room => {
                                if (!uniqueRooms.has(room.roomId)) {
                                    uniqueRooms.set(room.roomId, room);
                                }
                            });
                            return Array.from(uniqueRooms.values()).map((room) => {
                                const { owner, participant, participantAvatar, ownerAvatar } = room;
                                let otherUserNickname = "";
                                let imagePath = "";
                                if (owner === participant) {
                                    otherUserNickname = "나";
                                    imagePath = AVATAR_URL + myAvatarImageName;
                                } else if (myNickname === owner) {
                                    otherUserNickname = participant;
                                    imagePath = AVATAR_URL + participantAvatar;
                                } else if (myNickname === participant) {
                                    otherUserNickname = owner;
                                    imagePath = AVATAR_URL + ownerAvatar;
                                }
                                return (
                                    <List selection verticalAlign="middle" key={room.roomId}>
                                        <List.Item onClick={() => handleSelectRoom(room)}>
                                            <Image avatar src={imagePath} />
                                            <List.Content>
                                                <List.Description>
                                                    대화 상대: {otherUserNickname}
                                                </List.Description>
                                            </List.Content>
                                        </List.Item>
                                        <Button
                                            className={styles.leaveButton}
                                            onClick={() => leaveRoom(room.roomId)}
                                            size="small"
                                        >
                                            나가기
                                        </Button>
                                    </List>
                                );
                            });
                        })()}
                    </div>
                </Draggable>
            )}

            {selectedRoom && (
                <Chat
                    roomId={selectedRoom.roomId}
                    chatRoomName={
                        selectedRoom.owner === selectedRoom.participant
                            ? "나"
                            : myNickname === selectedRoom.owner
                                ? selectedRoom.participant
                                : myNickname === selectedRoom.participant
                                    ? selectedRoom.owner
                                    : selectedRoom.owner
                    }
                    setVisible={() => setSelectedRoom(null)}
                    setButtonVisible={setButtonVisible}
                />
            )}
        </div>
    );
};

export default ChatRoom;
