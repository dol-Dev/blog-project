import React, { useEffect, useState } from 'react';
import { Nav } from 'react-bootstrap';
import { toast } from 'react-toastify';
import { Button, Container, Dropdown, Icon, Input, Modal, Pagination, TextArea } from 'semantic-ui-react';
import Swal from 'sweetalert2';
import { useAuth } from '../../../contexts/AuthContext';
import axiosInstance from '../../../utils/axiosInstance';
import styles from './manageComment.module.css';

const ManageComment = () => {
    const [comments, setComments] = useState([]); // 서버에서 받은 계층형 데이터 (top-level만)
    const [searchKeyword, setSearchKeyword] = useState('');
    const [searchType, setSearchType] = useState('0');
    const [selectedComments, setSelectedComments] = useState([]);
    const [selectAll, setSelectAll] = useState(false);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [currentComment, setCurrentComment] = useState(null); // 답글 작성 시 부모 댓글
    const [commentContent, setCommentContent] = useState('');
    const [totalPages, setTotalPages] = useState(0);
    const [currentPage, setCurrentPage] = useState(0);
    const { authInfo } = useAuth();

    // 서버에서 계층형 댓글(최상위 댓글만 포함)을 조회
    const fetchComments = async (page) => {
        try {
            const response = await axiosInstance.get(`/api/comments/me?page=${page}`);
            if (response.status === 200) {
                setComments(response.data.data.content);
                setTotalPages(response.data.data.totalPages);
                setCurrentPage(page);
            }
        } catch (error) {
            console.error('Error fetching comments:', error);
        }
    };

    useEffect(() => {
        if (authInfo) {
            fetchComments(currentPage);
        }
    }, [authInfo, currentPage]);

    // 계층형 댓글 데이터를 flat list로 변환 (부모 댓글 후 이어서 답글)
    const flattenComments = (data) => {
        let flat = [];
        data.forEach((comment) => {
            flat.push(comment);
            if (comment.replies && comment.replies.length > 0) {
                // 답글은 그대로 flat 배열에 추가
                flat = flat.concat(comment.replies);
            }
        });
        return flat;
    };

    // flat list로 변환한 전체 댓글
    const flatComments = flattenComments(comments);

    // 전체 선택 토글 (flat list 기준)
    const handleSelectAll = (e) => {
        if (e.target.checked) {
            const allIds = flatComments.map((c) => c.id);
            setSelectedComments(allIds);
        } else {
            setSelectedComments([]);
        }
        setSelectAll(e.target.checked);
    };

    // 단일 댓글 선택 토글 (flat list)
    const handleSelectComment = (commentId) => {
        let updated;
        if (selectedComments.includes(commentId)) {
            updated = selectedComments.filter((id) => id !== commentId);
        } else {
            updated = [...selectedComments, commentId];
        }
        setSelectedComments(updated);
        setSelectAll(updated.length === flatComments.length);
    };

    // 단일 댓글 삭제
    const handleDeleteComment = async (commentId) => {
        const result = await Swal.fire({
            title: '정말 삭제하시겠습니까?',
            text: '삭제 후에는 복구할 수 없습니다!',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: '삭제',
            cancelButtonText: '취소',
        });
        if (result.isConfirmed) {
            try {
                const response = await axiosInstance.delete(`/api/comments/${commentId}`);
                if (response.status === 200) {
                    setComments(comments.filter((c) => c.id !== commentId));
                }
            } catch (error) {
                console.error('댓글 삭제 실패:', error);
                toast.error('댓글 삭제 실패');
            }
        }
    };

    // 벌크 삭제 처리
    const handleBulkDelete = async () => {
        const result = await Swal.fire({
            title: '정말 삭제하시겠습니까?',
            text: '삭제 후에는 복구할 수 없습니다!',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: '삭제',
            cancelButtonText: '취소',
        });
        if (result.isConfirmed) {
            try {
                await Promise.all(
                    selectedComments.map((id) => axiosInstance.delete(`/api/comments/${id}`))
                );
                fetchComments(currentPage);
                setSelectedComments([]);
                setSelectAll(false);
            } catch (error) {
                console.error('댓글 일괄 삭제 실패:', error);
                toast.error('댓글 삭제 실패');
            }
        }
    };

    // 답글 작성을 위한 모달 열기 (부모 댓글 설정)
    const handleReply = (comment) => {
        setCurrentComment(comment);
        setIsModalOpen(true);
    };

    const handleModalClose = () => {
        setIsModalOpen(false);
        setCommentContent('');
        setCurrentComment(null);
    };

    // 답글 작성
    const handleReplySubmit = async () => {
        try {
            const response = await axiosInstance.post(`/api/comments`, {
                postId: currentComment.post.id,
                parentId: currentComment.id,
                userId: authInfo.id,
                content: commentContent,
            });
            if (response.status === 200) {
                handleModalClose();
                fetchComments(currentPage);
            }
        } catch (error) {
            console.error('답글 작성 실패:', error);
            toast.error('답글 작성 실패');
        }
    };


    // 댓글 검색
    const handleSearch = async (page = currentPage) => {
        try {
            const response = await axiosInstance.get(`api/comments/search/me?page=${page}`, {
                params: {
                    keyword: searchKeyword,
                    type: searchType,
                },
                paramsSerializer: (params) =>
                    Object.entries(params)
                        .map(([key, value]) => `${key}=${encodeURIComponent(value)}`)
                        .join('&'),
            });
            if (response.status === 200) {
                setComments(response.data.data.content);
                setTotalPages(response.data.data.totalPages);
                setCurrentPage(page);
            }
        } catch {
            toast.error("검색 실패");
        }
    };

    const handleKeyDown = (e) => {
        if (e.key === 'Enter') {
            handleSearch(0);
        }
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        handleSearch(0);
    };

    const handlePageChange = (e, { activePage }) => {
        setCurrentPage(activePage - 1);
    };

    return (
        <div className={styles.manageComments}>
            <h2 className={styles.title}>
                <span>
                    댓글 관리 {flatComments && <span className={styles.count}>{flatComments.length}</span>}
                </span>
            </h2>

            <div className={styles.searchActionsContainer}>
                <form className={styles.searchForm} onSubmit={handleSubmit}>
                    <Nav className="mr-auto">
                        <Dropdown
                            button
                            className={styles.searchDropdown}
                            options={[
                                { key: 'title', text: '제목', value: '0' },
                                { key: 'content', text: '내용', value: '1' },
                                { key: 'titleAndContent', text: '제목+내용', value: '2' },
                            ]}
                            onChange={(e, { value }) => setSearchType(value)}
                            defaultValue="0"
                            text={
                                searchType === '0'
                                    ? '제목'
                                    : searchType === '1'
                                        ? '내용'
                                        : '제목+내용'
                            }
                        />
                        <Input
                            type="text"
                            className={styles.searchInput}
                            placeholder="댓글 관리에서 검색합니다."
                            value={searchKeyword}
                            onChange={(e) => setSearchKeyword(e.target.value)}
                            onKeyDown={handleKeyDown}
                        />
                        <Button type="submit" className={styles.searchButton}>
                            <Icon name="search" />
                        </Button>
                    </Nav>
                </form>

                <div className={styles.bulkActionsContainer}>
                    <Input
                        type="checkbox"
                        id="selectAll"
                        className={styles.allCheckbox}
                        onChange={handleSelectAll}
                        checked={selectAll}
                    />
                    <label htmlFor="selectAll" className={styles.checkboxLabel}>
                        {selectAll ? '모두 선택됨' : '선택 됨'}
                    </label>
                    <Dropdown
                        text="변경"
                        icon="caret down"
                        floating
                        labeled
                        button
                        className="icon"
                        disabled={selectedComments.length === 0}
                    >
                        <Dropdown.Menu>
                            <Dropdown.Item text="삭제" onClick={handleBulkDelete} />
                        </Dropdown.Menu>
                    </Dropdown>
                </div>
            </div>

            {/* Flat Comments List */}
            <div className={styles.commentsList}>
                {flatComments && flatComments.length > 0 ? (
                    flatComments.map((comment) => (
                        <div key={comment.id} className={styles.commentItem}>
                            <Input
                                type="checkbox"
                                className={styles.checkbox}
                                checked={selectedComments.includes(comment.id)}
                                onChange={() => handleSelectComment(comment.id)}
                            />
                            <div className={styles.commentDiv}>
                                <div className={styles.commentMeta}>
                                    <span className={styles.commentAuthor}>{comment.writer?.nickname}</span>
                                    <span className={styles.commentSeparator}>ㆍ</span>
                                    <span className={styles.commentDate}>
                                        {new Date(comment.createdAt).toLocaleString()}
                                    </span>
                                </div>
                                <div className={styles.commentContent}>
                                    {comment.parentId !== null && (
                                        <span className={styles.replyLabel}>[답글] </span>
                                    )}
                                    {comment.content}
                                </div>
                                <div className={styles.commentTitle}>
                                <Icon name="file alternate outline" />
                                {comment.post.title}
                                </div>
                            </div>
                            <div className={styles.commentActions}>
                                {comment.parentId === null && (
                                    <Button className={styles.commentAction} onClick={() => handleReply(comment)}>
                                        답글
                                    </Button>
                                )}
                                <Button className={styles.commentAction} onClick={() => handleDeleteComment(comment.id)}>
                                    삭제
                                </Button>
                            </div>
                        </div>
                    ))
                ) : (
                    <div className={styles.noComments}>아직 등록된 댓글이 없습니다</div>
                )}
            </div>

            <Container textAlign="center" className={styles.paginationContainer}>
                <Pagination
                    defaultActivePage={currentPage + 1}
                    totalPages={totalPages}
                    onPageChange={handlePageChange}
                    ellipsisItem={{ content: <Icon name="ellipsis horizontal" />, icon: true }}
                    firstItem={{ content: <Icon name="angle double left" />, icon: true }}
                    lastItem={{ content: <Icon name="angle double right" />, icon: true }}
                    prevItem={{ content: <Icon name="angle left" />, icon: true }}
                    nextItem={{ content: <Icon name="angle right" />, icon: true }}
                />
            </Container>

            <Modal open={isModalOpen} onClose={handleModalClose} size="small">
                <Modal.Header>답글 작성</Modal.Header>
                <Modal.Content>
                    <TextArea
                        placeholder="답글 내용을 입력하세요."
                        value={commentContent}
                        onChange={(e) => setCommentContent(e.target.value)}
                    />
                </Modal.Content>
                <Modal.Actions>
                    <Button onClick={handleModalClose}>취소</Button>
                    <Button primary onClick={handleReplySubmit}>
                        답글 달기
                    </Button>
                </Modal.Actions>
            </Modal>
        </div>
    );
};

export default ManageComment;
