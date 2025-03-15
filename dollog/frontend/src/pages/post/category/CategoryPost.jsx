import 'bootstrap/dist/css/bootstrap.min.css';
import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import 'semantic-ui-css/semantic.min.css';
import { Container, Divider, Icon, Item, Pagination } from 'semantic-ui-react';
import { useBlog } from '../../../contexts/BlogContext';
import axiosInstance from '../../../utils/axiosInstance';
import styles from './categoryPost.module.css';

const CategoryPost = () => {
    const [posts, setPosts] = useState([]);
    const [totalPages, setTotalPages] = useState(0);
    const [currentPage, setCurrentPage] = useState(0);
    const navigate = useNavigate();
    const { categoryId } = useParams();
    const { nickname, provider } = useBlog();

    useEffect(() => {
        if (nickname) {
            fetchPosts(currentPage, categoryId, nickname, provider);
        }
    }, [currentPage, categoryId, nickname, provider]);

    const fetchPosts = async (page, categoryId, nickname, provider) => {
        try {
            let url, params;

            if (categoryId === 'all' || categoryId === '' || typeof categoryId === 'undefined') {
                // 닉네임 기반 조회
                url = "/api/posts/nickname";
                params = { nickname, provider, page };
            } else {
                // 카테고리 기반 조회
                url = "/api/posts/category";
                params = { categoryId, page };
            }

            const response = await axiosInstance.get(url, { params });
            const responseData = response.data.data;
            setPosts(responseData.content);
            setTotalPages(responseData.totalPages);
            setCurrentPage(page);
        } catch (error) {
            console.error("Error fetching posts:", error);
        }
    };

    const handlePageClick = (pageNumber) => {
        setCurrentPage(pageNumber);
    };

    const stripHtmlTags = (html) => {
        return html.replace(/<[^>]+>/g, '');
    };

    const formatDate = (dateString) => {
        const date = new Date(dateString);
        const today = new Date();

        // 오늘 날짜인지 확인 (년, 월, 일)
        if (
            date.getFullYear() === today.getFullYear() &&
            date.getMonth() === today.getMonth() &&
            date.getDate() === today.getDate()
        ) {
            // 오늘이면 시간과 분만 표시 (시간과 분 사이에 공백 추가)
            const hours = date.getHours();
            const minutes = date.getMinutes();
            const period = hours < 12 ? '오전' : '오후';
            // 0시나 12시의 경우 12로 표시
            const adjustedHour = hours % 12 === 0 ? 12 : hours % 12;
            const formattedMinutes = minutes.toString().padStart(2, '0');
            return `${period} ${adjustedHour} : ${formattedMinutes}`;
        } else {
            // 오늘이 아닌 경우 년, 월, 일을 "YYYY년 M월 D일" 형식으로 표시
            const year = date.getFullYear();
            const month = date.getMonth() + 1;
            const day = date.getDate();
            return `${year}년 ${month}월 ${day}일`;
        }
    };


    return (
        <div className={styles['category-post-container']}>
            <div className={styles.divider}></div>
            <Item.Group className={styles['item-group']}>
                {posts.map(post => {
                    return (
                        <React.Fragment key={post.id}>
                            <Item className={styles.postItem}>
                                <Item.Content>
                                    <Item.Meta>
                                        {post.category.name}
                                    </Item.Meta>
                                    <Item.Header onClick={() => navigate(`/detail-post/${post.id}`)}>
                                        {post.title}
                                    </Item.Header>
                                    <Item.Meta>
                                        <span className={styles.author}>
                                            {post.nickname || '알 수 없음'}
                                        </span>
                                        <span className={styles['meta-separator']}>|</span>
                                        <span className={styles.date}>
                                            {formatDate(post.createDate)}
                                        </span>
                                    </Item.Meta>
                                    <Item.Description>
                                        {stripHtmlTags(post.content.length >= 70 ? `${post.content.substring(0, 69)}...` : post.content)}
                                    </Item.Description>
                                </Item.Content>
                            </Item>
                            <Divider className="post-divider" />
                        </React.Fragment>
                    );
                })}
            </Item.Group>

            <Container className={styles['pagination-container']}>
                <Pagination
                    defaultActivePage={currentPage + 1}
                    totalPages={totalPages}
                    onPageChange={(e, { activePage }) => handlePageClick(activePage - 1)}
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

export default CategoryPost;