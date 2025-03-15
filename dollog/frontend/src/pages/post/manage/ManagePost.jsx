import React, { useEffect, useState } from 'react';
import { Nav } from "react-bootstrap";
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { Button, Container, Dropdown, Icon, Input, Pagination } from 'semantic-ui-react';
import Swal from 'sweetalert2';
import { useAuth } from '../../../contexts/AuthContext';
import axiosInstance from '../../../utils/axiosInstance';
import styles from './managePost.module.css';

const ManagePost = () => {
    const [posts, setPosts] = useState([]);
    const [searchKeyword, setSearchKeyword] = useState('');
    const [searchType, setSearchType] = useState('0');
    const [totalPages, setTotalPages] = useState(0);
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPosts, setTotalPosts] = useState(0);
    const [selectedPosts, setSelectedPosts] = useState([]);
    const [selectAll, setSelectAll] = useState(false);
    const navigate = useNavigate();
    const { authInfo } = useAuth();


    useEffect(() => {
        fetchPosts(currentPage);
    }, [authInfo, currentPage]);

    const fetchPosts = async (page) => {
        try {
            const response = await axiosInstance.get(`/api/posts/me?page=${page}`);
            if (response.status === 200) {
                setPosts(response.data.data.content);
                setTotalPages(response.data.data.totalPages);
                setCurrentPage(page);
                setTotalPosts(response.data.data.totalElements);
            }
        } catch (error) {
            console.error('Error fetching posts:', error);
        }
    };

    const handleEditPost = (postId) => {
        navigate(`/update-post/${postId}`);
    };

    const handleDeletePost = async (postId) => {
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
                const response = await axiosInstance.delete(`/api/posts/${postId}`);
                if (response.status === 200) {
                    setPosts(posts.filter(post => post.id !== postId));
                    fetchPosts(currentPage);
                }
            } catch {
                toast.error("게시글 삭제 실패");
            }
        }
    };

    const handleBulkDelete = async (action) => {
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
                if (action === 'delete') {
                    const response = await Promise.all(selectedPosts.map(postId => axiosInstance.delete(`/api/posts/${postId}`)));
                    if (response.every(res => res.status === 200)) {
                        setPosts(posts.filter(post => !selectedPosts.includes(post.id)));
                        setSelectedPosts([]);
                        setSelectAll(false);
                        fetchPosts(currentPage);
                    }
                }
            } catch {
                toast.error("게시글 삭제 실패");
            }
        }
    };

    // 게시글 검색
    const handleSearch = async (page = currentPage) => {
        try {
            const response = await axiosInstance.get(`api/posts/search/me?page=${page}`, {
                params: {
                    keyword: searchKeyword,
                    type: searchType
                },
                paramsSerializer: params => {
                    return Object.entries(params).map(([key, value]) => `${key}=${encodeURIComponent(value)}`).join('&');
                }
            });
            if (response.status === 200) {
                setPosts(response.data.data.content);
                setTotalPages(response.data.data.totalPages);
                setCurrentPage(page);
            }
        } catch {
            toast.error("검색 실패");
        }
    };


    // 엔터 키를 감지하여 검색 기능 호출
    const handleKeyDown = (e) => {
        if (e.key === 'Enter') {
            handleSearch(0);
        }
    };

    // 폼 제출을 막고 검색 기능을 호출하는 함수
    const handleSubmit = (e) => {
        e.preventDefault();
        handleSearch(0);
    };

    const handlePageChange = (e, { activePage }) => {
        setCurrentPage(activePage - 1);
        handleSearch(activePage - 1);
    };

    const handleSelectAll = (e) => {
        if (e.target.checked) {
            setSelectedPosts(posts.map(post => post.id));
        } else {
            setSelectedPosts([]);
        }
        setSelectAll(e.target.checked);
    };

    const handleSelectPost = (postId) => {
        const updatedSelectedPosts = selectedPosts.includes(postId)
            ? selectedPosts.filter(id => id !== postId)
            : [...selectedPosts, postId];

        setSelectedPosts(updatedSelectedPosts);
        setSelectAll(updatedSelectedPosts.length === posts.length);
    };

    return (
        <div className={styles.managePosts}>
            <h2 className={styles.title}>
                <span>글 관리 {posts ? <span className={styles.count}>{totalPosts}</span> : null}</span>
                <Link to="/write-post" className={styles.writeLink}>
                    <Icon name="write" />
                </Link>
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
                                { key: 'titleAndContent', text: '제목+내용', value: '2' }
                            ]}
                            onChange={(e, { value }) => setSearchType(value)}
                            defaultValue='0'
                            text={
                                searchType === '0' ? '제목' :
                                    searchType === '1' ? '내용' :
                                        '제목+내용'
                            }
                        />
                        <Input
                            type="text"
                            className={styles.searchInput}
                            placeholder="글 관리에서 검색합니다."
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
                        text='변경'
                        icon='caret down'
                        floating
                        labeled
                        button
                        className='icon'
                        disabled={selectedPosts.length === 0}
                    >
                        <Dropdown.Menu>
                            <Dropdown.Item text='삭제' onClick={() => handleBulkDelete('delete')} />
                        </Dropdown.Menu>
                    </Dropdown>
                </div>
            </div>

            {/* Posts List */}
            <div className={styles.postsList}>
                {posts && posts.length > 0 ? (
                    posts.map(post => (
                        <div className={styles.postItem} key={post.id}>
                            <Input
                                type="checkbox"
                                className={styles.checkbox}
                                checked={selectedPosts.includes(post.id)}
                                onChange={() => handleSelectPost(post.id)}
                            />
                            <div className={styles.postDiv}>
                                <div className={styles.postMeta}>
                                    <span className={styles.postCategory}>{post.category?.name}</span>
                                    <span className={styles.postSeparator}>ㆍ</span>
                                    <span className={styles.postAuthor}>
                                        {post.nickname}
                                    </span>
                                    <span className={styles.postSeparator}>ㆍ</span>
                                    <span className={styles.postDate}>{new Date(post.createDate).toLocaleString()}</span>
                                </div>
                                <div className={styles.postTitle}>
                                    <a href={`/detail-post/${post.id}`}>{post.title}</a>
                                </div>

                            </div>
                            <div>
                                <Button onClick={() => handleEditPost(post.id)}>수정</Button>
                                <Button onClick={() => handleDeletePost(post.id)}>삭제</Button>
                            </div>
                        </div>
                    ))
                ) : (
                    <div className={styles.noPosts}>아직 등록된 게시글이 없습니다</div>
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
        </div>
    );
};

export default ManagePost;
