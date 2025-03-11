import React, { useEffect, useState } from "react";
import { toast } from "react-toastify";
import { Button, Comment, Form, Icon, Label } from "semantic-ui-react";
import Swal from "sweetalert2";
import AVATAR_URL from "../../utils/avatarUrl";
import axiosInstance from "../../utils/axiosInstance";
import styles from "./comment.module.css";

const CommentList = ({ postId, currentUserId }) => {
    const [comments, setComments] = useState([]);
    const [newCommentContent, setNewCommentContent] = useState("");

    useEffect(() => {
        fetchComments();
    }, [postId]);

    const fetchComments = async () => {
        try {
            const response = await axiosInstance.get(`/api/comments/post/${postId}`);
            if (response.status === 200) {
                setComments(response.data.data);
            }
        } catch (error) {
            console.error("Error fetching comments:", error);
        }
    };

    // 최상위 댓글 작성
    const handleCreateComment = async () => {
        if (!currentUserId) {
            toast.error("로그인 후 다시 시도해주세요.");
            return;
        }

        if (!newCommentContent.trim()) {
            toast.error("내용을 입력해주세요.");
            return;
        }

        try {
            const response = await axiosInstance.post("/api/comments", {
                userId: currentUserId,
                postId: postId,
                content: newCommentContent,
                parentId: null,
            });
            if (response.status === 200) {
                setNewCommentContent("");
                fetchComments();
            }
        } catch (error) {
            toast.error("등록 실패. 다시 시도해주세요.");
        }
    };

    return (
        <Comment.Group>
            {comments.map((comment) => (
                <SingleComment
                    key={comment.id}
                    comment={comment}
                    currentUserId={currentUserId}
                    postId={postId}
                    onRefreshComment={fetchComments}
                />
            ))}
            <Form reply className={styles.commentForm}>
                <Form.TextArea
                    placeholder="댓글을 입력하세요..."
                    value={newCommentContent}
                    onChange={(e) => setNewCommentContent(e.target.value)}
                />
                <Button
                    content="댓글 쓰기"
                    icon="edit"
                    color="blue"
                    onClick={handleCreateComment}
                />
            </Form>
        </Comment.Group>
    );
};

const SingleComment = ({ comment, postId, currentUserId, onRefreshComment }) => {
    const [isEditing, setIsEditing] = useState(false);
    const [editContent, setEditContent] = useState(comment.content);
    const [showReplyForm, setShowReplyForm] = useState(false);
    const [replyContent, setReplyContent] = useState("");
    const [showReplies, setShowReplies] = useState(false);

    // 댓글 수정
    const handleUpdate = async () => {
        if (!currentUserId) {
            toast.error("로그인 후 다시 시도해주세요.");
            return;
        }
        if (!editContent.trim()) {
            toast.error("내용을 입력해주세요.");
            return;
        }
        try {
            const response = await axiosInstance.put(`/api/comments/${comment.id}`, {
                content: editContent,
            });
            if (response.status === 200) {
                setIsEditing(false);
                onRefreshComment();
            }
        } catch (error) {
            toast.error("수정 실패. 다시 시도해주세요.");
        }
    };

    // 댓글 삭제
    const handleDelete = async () => {
        if (!currentUserId) {
            toast.error("로그인 후 다시 시도해주세요.");
            return;
        }
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
                const response = await axiosInstance.delete(`/api/comments/${comment.id}`);
                if (response.status === 200) {
                    onRefreshComment();
                }
            } catch (error) {
                toast.error("삭제 실패. 다시 시도해주세요.");
            }
        }
    };

    // 댓글 좋아요 추가
    const addLikeComment = async () => {
        try {
            const response = await axiosInstance.post(`/api/likes/comments/${comment.id}`);
            if (response.status === 200) {
                localStorage.setItem(`comment_${comment.id}_liked_${currentUserId}`, 'true');
                onRefreshComment();
            }
        } catch (error) {
            toast.error("좋아요 추가 실패. 다시 시도해주세요.");
        }
    };

    // 댓글 좋아요 취소
    const deleteLikeComment = async () => {
        try {
            const response = await axiosInstance.post(`/api/likes/comments/${comment.id}`);
            if (response.status === 200) {
                localStorage.setItem(`comment_${comment.id}_liked_${currentUserId}`, 'false');
                onRefreshComment();
            }
        } catch (error) {
            toast.error("좋아요 취소 실패. 다시 시도해주세요.");
        }
    };

    // 대댓글 작성 (최상위 댓글에만 적용)
    const handleCreateReply = async () => {
        if (!currentUserId) {
            toast.error("로그인 후 다시 시도해주세요.");
            return;
        }
        if (!replyContent.trim()) {
            toast.error("내용을 입력해주세요.");
            return;
        }
        try {
            const response = await axiosInstance.post("/api/comments", {
                userId: currentUserId,
                postId: postId,
                content: replyContent,
                parentId: comment.id,
            });
            if (response.status === 200) {
                setReplyContent("");
                setShowReplyForm(false);
                onRefreshComment();
            }
        } catch (error) {
            toast.error("등록 실패. 다시 시도해주세요.");
        }
    };

    const handleShowEdit = () => {
        setIsEditing(true);
        setEditContent(comment.content);
    };

    return (
        <Comment>
            <Comment.Content>
                <Comment.Author as="span">
                    <img
                        src={`${AVATAR_URL}${comment.writer.avatarImageName}`}
                        alt="Avatar"
                        className={styles.avatar}
                    />
                    {comment.writer?.nickname}
                </Comment.Author>

                <Comment.Metadata>
                    <span>
                        {new Date(comment.createdAt).toLocaleString("ko-KR", {
                            year: "numeric",
                            month: "short",
                            day: "numeric",
                            hour: "2-digit",
                            minute: "2-digit",
                        })}
                    </span>
                </Comment.Metadata>

                {/* 댓글 좋아요 토글 액션 */}
                <Comment.Metadata>
                    <span>
                        {localStorage.getItem(`comment_${comment.id}_liked_${currentUserId}`) === "true" ? (
                            <Button as="div" labelPosition="right" size="mini">
                                <Button icon color="red" onClick={deleteLikeComment} size="mini">
                                    <Icon name="heart" />
                                </Button>
                                <Label basic color="red" pointing="left">
                                    {comment.likeCnt || 0}
                                </Label>
                            </Button>
                        ) : (
                            <Button as="div" labelPosition="right" size="mini">
                                <Button icon onClick={addLikeComment} size="mini">
                                    <Icon name="heart" />
                                </Button>
                                <Label basic pointing="left">
                                    {comment.likeCnt || 0}
                                </Label>
                            </Button>
                        )}
                    </span>
                </Comment.Metadata>

                {isEditing ? (
                    <Form reply>
                        <Form.TextArea
                            value={editContent}
                            onChange={(e) => setEditContent(e.target.value)}
                        />
                        <Button
                            icon="edit"
                            content="수정 완료"
                            color="blue"
                            onClick={handleUpdate}
                            size="mini"
                        />
                        <Button
                            icon="cancel"
                            content="취소"
                            onClick={() => setIsEditing(false)}
                            size="mini"
                        />
                    </Form>
                ) : (
                    <>
                        <Comment.Text>{comment.content}</Comment.Text>
                        <Comment.Actions>
                            {comment.parentId === null && (
                                <>
                                    <Comment.Action onClick={() => setShowReplyForm(!showReplyForm)}>
                                        답글
                                    </Comment.Action>
                                    {comment.replies && comment.replies.length > 0 && (
                                        <Comment.Action onClick={() => setShowReplies(!showReplies)}>
                                            {showReplies ? "답글 숨기기" : `답글 보기 (${comment.replies.length})`}
                                        </Comment.Action>
                                    )}
                                </>
                            )}
                            {comment.writer?.userId === currentUserId && (
                                <>
                                    <Comment.Action onClick={handleShowEdit}>수정</Comment.Action>
                                    <Comment.Action onClick={handleDelete}>삭제</Comment.Action>
                                </>
                            )}
                        </Comment.Actions>
                    </>
                )}
            </Comment.Content>

            {showReplyForm && comment.parentId === null && !isEditing && (
                <Form reply className={styles.replyForm}>
                    <Form.TextArea
                        placeholder="대댓글을 입력하세요..."
                        value={replyContent}
                        onChange={(e) => setReplyContent(e.target.value)}
                    />
                    <Button
                        content="대댓글 작성"
                        icon="edit"
                        color="grey"
                        onClick={handleCreateReply}
                        size="mini"
                    />
                </Form>
            )}

            {showReplies && comment.replies && comment.replies.length > 0 && (
                <Comment.Group className={styles.replyGroup}>
                    {comment.replies.map((child) => (
                        <SingleComment
                            key={child.id}
                            comment={child}
                            currentUserId={currentUserId}
                            onRefreshComment={onRefreshComment}
                        />
                    ))}
                </Comment.Group>
            )}
        </Comment>
    );
};

export default CommentList;
