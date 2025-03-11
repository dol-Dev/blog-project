import { Client } from '@stomp/stompjs';
import dayjs from 'dayjs';
import 'dayjs/locale/ko'; // 한국어 로케일 추가
import customParseFormat from 'dayjs/plugin/customParseFormat';
import localizedFormat from 'dayjs/plugin/localizedFormat';
import timezone from 'dayjs/plugin/timezone';
import utc from 'dayjs/plugin/utc';
import React, { useEffect, useRef, useState } from 'react';
import Draggable from 'react-draggable';
import { Link } from 'react-router-dom';
import { Button, Divider, Icon } from 'semantic-ui-react';
import SockJS from 'sockjs-client';
import { useAuth } from '../../../contexts/AuthContext';
import AVATAR_URL from '../../../utils/avatarUrl';
import axiosInstance from '../../../utils/axiosInstance';
import BASE_URL from '../../../utils/baseUrl';
import styles from './chat.module.css';

dayjs.extend(localizedFormat);
dayjs.extend(utc);
dayjs.extend(timezone);
dayjs.extend(customParseFormat);
dayjs.locale('ko');

const Chat = ({ roomId, chatRoomName, setVisible, setButtonVisible }) => {
    const [message, setMessage] = useState('');
    const [messages, setMessages] = useState([]);
    const [roomName, setRoomName] = useState(chatRoomName);
    const [nickname, setNickname] = useState('');
    const clientRef = useRef(null);
    const [scrollPosition, setScrollPosition] = useState(0);
    const [chatPosition, setChatPosition] = useState({ x: 0, y: 0 });
    const [dragging, setDragging] = useState(false);
    const [darkMode, setDarkMode] = useState(false);
    const messageListRef = useRef(null);
    const { authInfo } = useAuth();

    // 인증 정보 설정
    useEffect(() => {
        if (authInfo) {
            setNickname(authInfo.nickname);
        }
    }, [authInfo]);

    // 채팅 기록 불러오기
    useEffect(() => {
        const fetchChatHistory = async () => {
            try {
                const response = await axiosInstance.get(`/api/chat/${roomId}/messages`);
                if (response.status === 200) {
                    const chatMessageDtoList = response.data.data;
                    setMessages(Array.isArray(chatMessageDtoList) ? chatMessageDtoList : []);
                }
            } catch (error) {
                console.error('Fail to fetching chat history :', error);
                setMessages([]);
            }
        };
        fetchChatHistory();
    }, [roomId]);

    // STOMP 연결 설정
    useEffect(() => {
        let subscription;
        const socket = new SockJS(BASE_URL + '/ws', null, { withCredentials: true });
        const client = new Client({
            webSocketFactory: () => socket,
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
            onConnect: (frame) => {
                subscription = client.subscribe(`/topic/rooms/${roomId}`, (message) => {
                    const messageData = JSON.parse(message.body);
                    setMessages(prevMessages => [...prevMessages, messageData]);
                });
            },
            onStompError: (frame) => {
                console.error("STOMP error", frame);;
            }
        });

        clientRef.current = client;
        client.activate();

        return () => {
            if (subscription) {
                subscription.unsubscribe();
            }
            client.deactivate();
        };
    }, [roomId]);

    // 채팅방 이름 업데이트
    useEffect(() => {
        setRoomName(chatRoomName);
    }, [chatRoomName]);

    // 기본 위치 설정
    useEffect(() => {
        setChatPosition({ x: 30, y: 30 });
    }, []);

    // 스크롤 위치 변화에 따라 채팅창의 y 위치 업데이트
    useEffect(() => {
        setChatPosition((prevPosition) => ({
            ...prevPosition,
            y: scrollPosition,
        }));
    }, [scrollPosition]);

    // window 스크롤 이벤트 리스너 추가 (scrollPosition 업데이트)
    useEffect(() => {
        const handleScroll = () => {
            const scrollTop = window.scrollY;
            setScrollPosition(scrollTop * 1.17);
        };
        window.addEventListener('scroll', handleScroll);
        return () => {
            window.removeEventListener('scroll', handleScroll);
        };
    }, []);

    // ESC 키로 채팅창 닫기
    useEffect(() => {
        const handleKeyDown = (event) => {
            if (event.key === 'Escape') {
                setVisible(); // 채팅창 닫기
                setButtonVisible(true); // 버튼 다시 보이기
            }
        };
        window.addEventListener('keydown', handleKeyDown);
        return () => {
            window.removeEventListener('keydown', handleKeyDown);
        };
    }, [setVisible, setButtonVisible]);


    // 메시지 목록 스크롤 하단으로 이동
    useEffect(() => {
        scrollToBottom();
    }, [messages]);

    const scrollToBottom = () => {
        if (messageListRef.current) {
            messageListRef.current.scrollTop = messageListRef.current.scrollHeight;
        }
    };

    // 메세지 전송
    const sendMessage = () => {
        if (!clientRef.current || !clientRef.current.connected) {
            console.error("STOMP 연결이 활성화되지 않았습니다.");
            return;
        }

        if (message.trim() !== '') {
            const messageData = {
                roomId: roomId,
                message: message,
                sender: nickname
            };

            try {
                clientRef.current.publish({
                    destination: `/app/chat/${roomId}/send`,
                    body: JSON.stringify(messageData),
                });
                setMessage('');
                scrollToBottom();
            } catch (error) {
                console.error("메시지 전송 실패:", error);
            }
        }
    };

    // Enter키 입력시 메시지 전송
    const handleKeyPress = (event) => {
        if (event.key === 'Enter') {
            sendMessage();
        }
    };

    // 드래그 시작
    const handleStart = () => {
        setDragging(true);
    };

    // 드래그 중
    const handleDrag = (e, data) => {
        if (dragging) {
            // deltaX와 deltaY 값을 즉시 반영하여 채팅방 위치 업데이트
            const newX = chatPosition.x + data.deltaX;
            const newY = chatPosition.y + data.deltaY;
            setChatPosition({ x: newX, y: newY });
        }
    };

    // 드래그 멈춤
    const handleStop = (e, data) => {
        setChatPosition({ x: data.x, y: data.y });
        setDragging(false);
    };

    // 날짜 포맷 변경 및 구분선 생성
    const formatDateHeader = (date) => {
        return (
            <Divider horizontal>
                {dayjs(date).locale('ko').format('YYYY년 M월 D일 dddd')}
            </Divider>
        );
    };

    // 메시지 그룹화 및 렌더링
    const renderMessages = () => {
        const groupedMessages = messages.reduce((acc, msg) => {
            const date = dayjs(msg.createdAt).format('YYYY-MM-DD');
            if (!acc[date]) {
                acc[date] = [];
            }
            acc[date].push(msg);
            return acc;
        }, {});

        return Object.entries(groupedMessages).map(([date, msgs]) => (
            <div key={date}>
                <div className={styles['date-header']}>{formatDateHeader(date)}</div>
                {msgs.map((msg, index) => {
                    const isOutgoing = (msg.sender === nickname);
                    const senderName = msg.sender;
                    const senderAvatar = (msg.sender === msg.owner) ? msg.ownerAvatar : msg.participantAvatar;
                    const formattedTime = dayjs(msg.createdAt)
                        .tz('Asia/Seoul')
                        .format('HH:mm');

                    return (
                        <div
                            key={index}
                            className={`${styles.message} ${isOutgoing ? styles['message-outgoing'] : styles['message-incoming']
                                }`}
                        >
                            {/* 왼쪽 아바타 (incoming) */}
                            {!isOutgoing && (
                                <Link
                                    to={`/blog/${senderName}`}
                                    state={{ blogName: msg.blogName, provider: msg.provider }}
                                >
                                    <img
                                        src={AVATAR_URL + senderAvatar}
                                        alt="Avatar"
                                        className={styles.avatar}
                                    />
                                </Link>
                            )}
                            <div className={styles['message-sender']}>
                                {decodeURIComponent(senderName)}
                            </div>
                            <div className={styles['message-body']}>
                                <div className={styles['message-content']}>
                                    <div className={styles['message-text']}>{msg.message}</div>
                                    <div className={styles['message-time']}>
                                        {formattedTime}
                                    </div>
                                </div>
                            </div>
                            {/* 오른쪽 아바타 (outgoing) */}
                            {isOutgoing && (
                                <img
                                    src={AVATAR_URL + senderAvatar}
                                    alt="Avatar"
                                    className={styles.avatar}
                                />
                            )}
                        </div>
                    );
                })}
            </div>
        ));
    };

    return (
        <Draggable position={chatPosition} onStart={handleStart} onDrag={handleDrag} onStop={handleStop}>
            <div className={`${styles['chat-container']} ${darkMode ? styles['dark-mode'] : ''}`}>
                {/* 채팅 헤더 */}
                <div className={styles['chat-header']}>
                    <h2>{decodeURIComponent(roomName)}</h2>
                    <div className={styles['dark-mode-toggle']}>
                        <Button className={styles['dark-mode-button']} icon onClick={() => setDarkMode(!darkMode)}>
                            <Icon name={darkMode ? 'sun' : 'moon'} />
                        </Button>
                    </div>
                    <Button className={styles['close-button']} icon onClick={() => { setVisible(); setButtonVisible(true); }}>
                        <Icon name="close" />
                    </Button>
                </div>
                {/* 채팅 내용 */}
                <div className={styles['chat-content']}>
                    <div className={styles['message-list']} ref={messageListRef}>
                        {renderMessages()}
                    </div>
                    {/* 메시지 입력 */}
                    <div className={styles['message-input-container']}>
                        <input
                            type="text"
                            placeholder="메시지 입력..."
                            value={message}
                            onChange={(e) => setMessage(e.target.value)}
                            onKeyPress={handleKeyPress}
                            className={styles['message-input']}
                        />
                        <Button icon className={styles['send-button']} onClick={sendMessage}>
                            <Icon name="send" />
                        </Button>
                    </div>
                </div>
            </div>
        </Draggable>
    );
};

export default Chat;