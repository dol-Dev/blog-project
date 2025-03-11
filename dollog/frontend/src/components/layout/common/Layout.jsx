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

const Layout = ({ children }) => {
    const [blogSidebarVisible, setBlogSidebarVisible] = useState(false);
    const [chatRoomInfo] = useState({ roomId: null, roomName: "" });

    const [layoutInfoByPrincipal, setLayoutInfoByPrincipal] = useState({ bannerImageUrl: '', bannerDescription: '', blogName: '' });
    const [layoutInfoByNickname, setLayoutInfoByNickname] = useState({ bannerImageUrl: '', bannerDescription: '', blogName: '', nickname: '' });
    const { blogName, nickname } = useBlog();

    const { authInfo } = useAuth();
    const location = useLocation();

    // 공통 API 호출 함수
    const fetchLayoutInfo = async (url, includeNickname = true) => {
        try {
            const response = await axiosInstance.get(url);
            if (response.status === 200 && response.data.data) {
                const data = {
                    bannerImageUrl: response.data.data.bannerImageUrl,
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


    const handleBlogSidebarToggle = () => {
        setBlogSidebarVisible(!blogSidebarVisible);
    };

    const isAdminRoute = location.pathname.startsWith('/manage');


    return (
        <div className={styles.layout}>
            <Header onBlogSidebarToggle={handleBlogSidebarToggle} />
            <SemanticSidebar.Pushable>
                <Sidebar nickname={layoutInfoByNickname.nickname} visible={blogSidebarVisible} onClose={() => setBlogSidebarVisible(false)} />
                <SemanticSidebar.Pusher>
                    {blogName === '' ? (
                        <div className={styles.banner}>
                            <h1>Welcome to the Blog!</h1>
                            <p>Please register or log in to enjoy exciting blog activities.</p>
                        </div>
                    ) : layoutInfoByPrincipal.blogName === blogName ? (
                        <div className={styles.banner}>
                            <h1>{blogName} Blog</h1>
                            <img src={layoutInfoByPrincipal.bannerImageUrl} alt="Banner" className={styles.bannerImage} />
                            <p>{layoutInfoByPrincipal.bannerDescription}</p>
                        </div>
                    ) : (
                        <div className={styles.banner}>
                            <h1>{blogName} Blog</h1>
                            <img src={layoutInfoByNickname.bannerImageUrl} alt="Banner" className={styles.bannerImage} />
                            <p>{layoutInfoByNickname.bannerDescription}</p>
                        </div>
                    )}
                    <main className={styles.mainContent}>
                        {authInfo && !isAdminRoute && <ChatRoom/>}
                        {children}
                    </main>
                </SemanticSidebar.Pusher>
            </SemanticSidebar.Pushable>
            <Footer />
        </div>
    );
};

export default Layout;
