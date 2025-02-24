import 'bootstrap/dist/css/bootstrap.min.css';
import DOMPurify from 'dompurify';
import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import 'semantic-ui-css/semantic.min.css';
import { Container, Divider, Icon, Item, Pagination } from 'semantic-ui-react';
import axiosInstance from '../../utils/axiosInstance';
import styles from './index.module.css';

const DEFAULT_THUMBNAIL = 'https://i.namu.wiki/i/_FIKQ7NQtBilT8QtmXWvjY8FfusWX6uYHmoDPsK70tP_vijKovxuPJrT-oEEdhjlXPRCEJy0zR30MwQpVRQ0WA.webp';

const Index = () => {
    const [posts, setPosts] = useState([]);
    const [totalPages, setTotalPages] = useState(0);
    const [currentPage, setCurrentPage] = useState(0);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchPosts = async (page) => {
            try {
                const response = await axiosInstance.get(`http://localhost:8080/api/posts?page=${page}`);

                console.log("📢 API 응답 데이터:", response.data.data); 

                setPosts(response.data.data.content);
                setTotalPages(response.data.data.totalPages);
                setCurrentPage(page);
            } catch (error) {
                console.error('Error fetching posts:', error);
            }
        };

        fetchPosts(currentPage);
    }, [currentPage]);

    useEffect(() => {
        localStorage.removeItem('searchType');
        localStorage.removeItem('searchKeyword');
    }, []);

    const handlePageClick = (pageNumber) => {
        setCurrentPage(pageNumber);
    };

    if (!posts) {
        return <div>Loading...</div>;
    }

    const getThumbnailAndText = (content) => {
        const cleanContent = DOMPurify.sanitize(content, { USE_PROFILES: { html: true } });
        const parser = new DOMParser();
        const doc = parser.parseFromString(cleanContent, 'text/html');

        const imgTag = doc.querySelector('img');
        const imgSrc = imgTag ? imgTag.src : DEFAULT_THUMBNAIL;

        if (imgTag) {
            imgTag.remove();
        }
        const textContent = doc.body.textContent || "";

        return { imgSrc, textContent };
    };

    return (
            <Container>
                <Item.Group className={styles['item-group']}>
                    {posts.map((post) => {
                        const { imgSrc, textContent } = getThumbnailAndText(post.content);
                        return (
                            <React.Fragment key={post.id}>
                                <Item
                                    onClick={() => navigate(`/detail-post/${post.id}`)}
                                >
                                    {imgSrc ? (
                                        <img
                                            src={imgSrc}
                                            className={styles['thumbnail']}
                                            alt="thumbnail"
                                        />
                                    ) : (
                                        <img
                                            src="/default-thumbnail.jpg"
                                            className={styles['thumbnail']}
                                            alt="default thumbnail"
                                        />
                                    )}
                                    <Item.Content>
                                        <Item.Header>
                                            {post.title}
                                        </Item.Header>
                                        <Item.Description>
                                            {textContent.length >= 15 ? `${textContent.substring(0, 14)}...` : textContent}
                                        </Item.Description>
                                        <Item.Meta>
                                            <span className={styles['author']}>
                                                작성자 : {post.nickname || '알 수 없음'}
                                            </span>
                                            <span className={styles['meta-separator']}>|</span>
                                            <span>
                                                작성일 : {new Date(post.createDate).toLocaleDateString('ko-KR', {
                                                    year: 'numeric',
                                                    month: 'short',
                                                    day: 'numeric',
                                                    hour: 'numeric',
                                                    minute: 'numeric',
                                                })}
                                            </span>
                                        </Item.Meta>
                                    </Item.Content>
                                </Item>
                                <Divider className={styles['divider']} />
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
            </Container>
    );
};

export default Index;

