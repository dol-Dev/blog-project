import React, { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
import { Sidebar as SemanticSidebar } from 'semantic-ui-react';
import { useAuth } from '../../../contexts/AuthContext';
import { useBlog } from '../../../contexts/BlogContext';
import ChatRoom from '../../../pages/chat/chatRoom/ChatRoom';
import axiosInstance from '../../../utils/axiosInstance';
import Footer from '../../footer/Footer';
import Header from '../../header/Header';
import Sidebar from '../../sidebar/common/Sidebar';
import styles from './layout.module.css';
import BANNER_URL from '../../../utils/bannerUrl';

const Layout = ({ children }) => {

    const [isLargeScreen, setIsLargeScreen] = useState(window.innerWidth >= 1600);
    const [blogSidebarVisible, setBlogSidebarVisible] = useState(window.innerWidth >= 1600);

    const [layoutInfoByPrincipal, setLayoutInfoByPrincipal] = useState({ bannerImage: '', bannerDescription: '', blogName: '' });
    const [layoutInfoByNickname, setLayoutInfoByNickname] = useState({ bannerImage: '', bannerDescription: '', blogName: '', nickname: '' });
    const { blogName, nickname } = useBlog();

    const { authInfo } = useAuth();
    const location = useLocation();

    // 공통 API 호출 함수
    const fetchLayoutInfo = async (url, includeNickname = true) => {
        try {
            const response = await axiosInstance.get(url);
            if (response.status === 200 && response.data.data) {
                const data = {
                    bannerImage: BANNER_URL + response.data.data.bannerImageName,
                    bannerDescription: response.data.data.bannerDescription,
                    blogName: response.data.data.blogName,
                };
                if (includeNickname) {
                    data.nickname = response.data.data.nickname;
                }
                return data;
            }
        } catch (error) {
            console.error('Failed to fetch layout info:', error);
        }
        return null;
    };

    // principal 전용 함수 (로그인 o)
    const fetchLayoutInfoByPrincipal = async () => {
        const layoutInfo = await fetchLayoutInfo('/api/banners', false);
        if (layoutInfo) {
            setLayoutInfoByPrincipal(layoutInfo); //nickname 미포함
        }
    };

    // nickname 전용 함수 (로그인 유무 x)
    const fetchLayoutInfoByNickname = async () => {
        const layoutInfo = await fetchLayoutInfo(`/api/banners/${nickname}`);
        if (layoutInfo) {
            setLayoutInfoByNickname(layoutInfo); //nickname 포함
        }
    };


    useEffect(() => {
        if (blogName == authInfo?.blogName) {
            fetchLayoutInfoByPrincipal();
        }

        if (nickname) {
            fetchLayoutInfoByNickname();
        }
    }, [blogName]);

    useEffect(() => {
        const handleResize = () => {
            if (window.innerWidth >= 1600) {
                setIsLargeScreen(true);
                setBlogSidebarVisible(true);
            } else {
                setIsLargeScreen(false);
                setBlogSidebarVisible(false);
            }
        };
        handleResize();
        window.addEventListener('resize', handleResize);
        return () => window.removeEventListener('resize', handleResize);
    }, []);


    const handleBlogSidebarToggle = () => {
        if (isLargeScreen) {
            setBlogSidebarVisible(prev => !prev);
        }
    };

    const isAdminRoute = location.pathname.startsWith('/manage');
    const showSidebar = location.pathname.startsWith('/posts');


    return (
        <div>
            <Header onBlogSidebarToggle={handleBlogSidebarToggle} isLargeScreen={isLargeScreen} />
            <SemanticSidebar.Pushable>
                {isLargeScreen && showSidebar && (
                    <Sidebar
                        nickname={layoutInfoByNickname.nickname}
                        visible={blogSidebarVisible}
                        onClose={() => setBlogSidebarVisible(false)}

                    />
                )}
                <SemanticSidebar.Pusher>
                    {blogName === '' ? (
                        <div className={styles.banner}>
                            <h1>Welcome to the Blog!</h1>
                            <p>Please register or log in to enjoy exciting blog activities.</p>
                        </div>
                    ) : layoutInfoByPrincipal.blogName === blogName ? (
                        <div className={styles.banner}>
                            <h1>{blogName} Blog</h1>
                            <img src={layoutInfoByPrincipal.bannerImage} alt="Banner" className={styles.bannerImage} />
                            <p>{layoutInfoByPrincipal.bannerDescription}</p>
                        </div>
                    ) : (
                        <div className={styles.banner}>
                            <h1>{blogName} Blog</h1>
                            <img src={layoutInfoByNickname.bannerImage} alt="Banner" className={styles.bannerImage} />
                            <p>{layoutInfoByNickname.bannerDescription}</p>
                        </div>
                    )}
                    <main className={styles.mainContent}>
                        {authInfo && !isAdminRoute && <ChatRoom />}
                        {children}
                    </main>
                </SemanticSidebar.Pusher>
            </SemanticSidebar.Pushable>
            <Footer />
        </div>
    );
};

export default Layout;
