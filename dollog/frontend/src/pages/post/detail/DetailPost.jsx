import DOMPurify from "dompurify";
import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { toast } from "react-toastify";
import { Button, Container, Divider, Dropdown, Icon, Label } from "semantic-ui-react";
import Swal from "sweetalert2";
import { useAuth } from "../../../contexts/AuthContext";
import AVATAR_URL from "../../../utils/avatarUrl";
import axiosInstance from "../../../utils/axiosInstance";
import CommentList from "../../comment/common/Comment";
import styles from "./detailPost.module.css";
import { useChat } from "../../../contexts/ChatContext";

const DetailPost = () => {
    const { id } = useParams();
    const navigate = useNavigate();

    const { authInfo } = useAuth();
    const { setChatRoomInfo, setButtonVisible, setChatVisible } = useChat(); 

    const [detailPost, setDetailPost] = useState({});
    const [userId, setUserId] = useState('');
    const [nickname, setNickname] = useState('');
    const [blogName, setBlogName] = useState('');
    const [provider, setProvider] = useState('');

    useEffect(() => {
            setUserId(authInfo?.id);
            setNickname(authInfo?.nickname);
            setBlogName(authInfo?.blogName);
            setProvider(authInfo?.provider);
        fetchPost();
    }, [authInfo]);

    // 게시글 조회
    const fetchPost = async () => {
        try {
            const response = await axiosInstance.get(`/api/posts/${id}`);
            setDetailPost(response.data.data);
        } catch (error) {
            console.error('Error fetching post:', error);
        }
    };

    if (!detailPost.title && !detailPost.content && !id) {
        return toast.info("Loading...");
    }

    // 게시글 삭제
    const handleDeletePost = async () => {
        const result = await Swal.fire({
            title: "정말 삭제하시겠습니까?",
            text: "삭제 후에는 복구할 수 없습니다!",
            icon: "warning",
            showCancelButton: true,
            confirmButtonText: "삭제",
            cancelButtonText: "취소",
        });
        if (result.isConfirmed) {
            try {
                await axiosInstance.delete(`/api/posts/${id}`);
                navigate(`/blog/${nickname}`, { state: { blogName, provider } });
            } catch (error) {
                toast.error("삭제 실패. 다시 시도해주세요.");
            }
        }
    };

    // 좋아요 추가/취소 함수는 기존 코드 유지
    const addLikePost = async () => {
        try {
            const response = await axiosInstance.post(`/api/likes/posts/${id}`);
            if (response.status === 200) {
                localStorage.setItem(`post_${id}_liked_${userId}`, 'true');
                await fetchPost();
            }
        } catch (error) {
            toast.error("추가 실패. 다시 시도해주세요.");
        }
    };

    const deleteLikePost = async () => {
        try {
            const response = await axiosInstance.post(`/api/likes/posts/${id}`);
            if (response.status === 200) {
                localStorage.setItem(`post_${id}_liked_${userId}`, 'false');
                await fetchPost();
            }
        } catch (error) {
            toast.error("취소 실패. 다시 시도해주세요.");
        }
    };

    // 채팅방 진입(없으면 생성 후 진입)
    const handleChatRoomEntry = async () => {
        try {
            const response = await axiosInstance.get('/api/chatRooms/myRooms');
            const rooms = response.data.data || [];

            const existingRoom = rooms.find(room =>
                (room.owner === detailPost.nickname && room.participant === nickname) ||
                (room.owner === nickname && room.participant === detailPost.nickname)
            );

            if (existingRoom) {
                if (existingRoom.participantStatus === "LEFT") {
                    const reenterResponse = await axiosInstance.post(
                        `/api/chatRooms/${existingRoom.roomId}/reenter`,
                        null,
                        { params: { participant: nickname } }
                    );
                    setChatRoomInfo({
                        roomId: reenterResponse.data.data.roomId,
                        roomName: reenterResponse.data.data.owner,
                    });
                } else {
                    setChatRoomInfo({
                        roomId: existingRoom.roomId,
                        roomName: existingRoom.owner,
                    });
                }
            } else {
                const createResponse = await axiosInstance.post('/api/chatRooms/between', {
                    owner: detailPost.nickname,
                    participant: nickname
                });
                setChatRoomInfo({
                    roomId: createResponse.data.data.roomId,
                    roomName: createResponse.data.data.owner,
                });
            }
            setButtonVisible(false); 
            setChatVisible(true);
        } catch (error) {
            console.error("Chat room entry error: ", error);
            toast.error("채팅방 진입 실패. 다시 시도해주세요.");
        }
    };

    return (
        <Container>
            <div className={styles['container']}>
                {detailPost.nickname && (
                    <>
                        <div className={styles['user-info-container']}>
                            <Dropdown
                                trigger={
                                    <img
                                        src={`${AVATAR_URL}${detailPost.avatarImageName}`}
                                        alt="Avatar"
                                        className={styles['avatar']}
                                    />
                                }
                                pointing="right"
                                icon={null}
                            >
                                <Dropdown.Menu>
                                    {/* 1:1 대화 클릭 시 handleChatRoomEntry 호출 */}
                                    <Dropdown.Item
                                        text="1:1 대화"
                                        icon="chat"
                                        onClick={handleChatRoomEntry}
                                    />
                                    <Dropdown.Item
                                        text="블로그"
                                        icon="book"
                                        onClick={() => navigate(`/blog/${detailPost.nickname}`, {
                                            state: {
                                                blogName: detailPost.blogName,
                                                provider: detailPost.provider
                                            }
                                        })}
                                    />
                                </Dropdown.Menu>
                            </Dropdown>
                            <div className={styles['user-info']}>
                                {detailPost.nickname}
                            </div>
                            {nickname && nickname === detailPost.nickname && (
                                <Dropdown
                                    icon="ellipsis vertical"
                                    className={styles['kebob-dropdown']}
                                    pointing="right">
                                    <Dropdown.Menu>
                                        <>
                                            <Dropdown.Item
                                                text="수정"
                                                icon="edit"
                                                onClick={() => navigate(`/update-post/${id}`)}
                                            />
                                            <Dropdown.Item
                                                text="삭제"
                                                icon="trash alternate"
                                                onClick={handleDeletePost}
                                            />
                                        </>
                                    </Dropdown.Menu>
                                </Dropdown>
                            )}
                        </div>
                    </>
                )}
                <h1 className={styles['title']}>{detailPost.title}</h1>
                <br />
                <div className={styles['category-label']}>
                    {detailPost.category && (
                        <Label tag>
                            {detailPost.category.name}
                        </Label>
                    )}
                </div>
                <Divider />
                <div className={styles['content']}>
                    <div dangerouslySetInnerHTML={{ __html: DOMPurify.sanitize(detailPost.content) }} />
                </div>
                <br /><br /><br /><br /><br /><br />
                <div className="ui labeled button" tabIndex="0">
                    {localStorage.getItem(`post_${id}_liked_${userId}`) === "true" ? (
                        <Button as='div' labelPosition='right'>
                            <Button icon color='red' onClick={deleteLikePost}>
                                <Icon name='heart' />
                            </Button>
                            <Label basic color='red' pointing='left'>
                                {detailPost.likeCnt}
                            </Label>
                        </Button>
                    ) : (
                        <Button as='div' labelPosition='right'>
                            <Button icon onClick={addLikePost}>
                                <Icon name='heart' />
                            </Button>
                            <Label basic pointing='left'>
                                {detailPost.likeCnt}
                            </Label>
                        </Button>
                    )}
                </div>
                <Divider />
                <CommentList postId={id} currentUserId={userId} />
            </div>
        </Container>
    );
};

export default DetailPost;
