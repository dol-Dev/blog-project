import React, { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';

import { Sidebar as SemanticSidebar } from 'semantic-ui-react';
import { useBlog } from '../../../contexts/BlogContext';
import Footer from '../../footer/Footer';
import Header from '../../header/Header';
import styles from './layout.module.css';

import Sidebar from '../../../components/sidebar/common/Sidebar';
import axiosInstance from '../../../utils/axiosInstance';

const Layout = ({ children }) => {
    const [blogSidebarVisible, setBlogSidebarVisible] = useState(false);
    const [layoutInfoByPrincipal, setLayoutInfoByPrincipal] = useState({ bannerImageUrl: '', bannerDescription: '', username: '' });
    const [layoutInfoByNickname, setLayoutInfoByNickname] = useState({ bannerImageUrl: '', bannerDescription: '', username: '', userId: '' });
    const { nickname, blogName } = useBlog();
    const location = useLocation();

    useEffect(() => {
        if (blogName) {
            const fetchLayoutInfo = async (url, includeUserId = true) => {
                try {
                    const response = await axiosInstance.get(url);
                    if (response.status === 200 && response.data.data) {
                        const data = {
                            bannerImageUrl: response.data.data.bannerImageUrl,
                            bannerDescription: response.data.data.bannerDescription,
                            blogName: response.data.data.blogName,
                        };
                        if (includeUserId) {
                            data.userId = response.data.data.userId;
                        }
                        return data;
                    }
                } catch (error) {
                    console.error('Failed to fetch layout info:', error);
                }
                return null;
            };

            const fetchLayoutInfoByPrincipal = async () => {
                const layoutInfo = await fetchLayoutInfo('/api/banners', false); // userId 제외
                if (layoutInfo) {
                    setLayoutInfoByPrincipal(layoutInfo);
                }
            };

            const fetchLayoutInfoByNickname = async () => {
                if (blogName === '') return;
                const layoutInfo = await fetchLayoutInfo(`/api/banners/${nickname}`); // userId 포함
                if (layoutInfo) {
                    setLayoutInfoByNickname(layoutInfo);
                }
            };

            fetchLayoutInfoByPrincipal();
            fetchLayoutInfoByNickname();
        }
    }, [blogName]);

    const handleBlogSidebarToggle = () => {
        setBlogSidebarVisible(!blogSidebarVisible);
    };

    const isAdminRoute = location.pathname.startsWith('/admin'); // 현재 경로가 /admin 하위인지 확인

    return (
        <div className={styles.layout}>
            <Header onBlogSidebarToggle={handleBlogSidebarToggle} isBlogSidebarVisible={blogSidebarVisible} />
            <SemanticSidebar.Pushable>
                <Sidebar userId={layoutInfoByNickname.userId} visible={blogSidebarVisible} onClose={() => setBlogSidebarVisible(false)} />
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
                        {/* /admin 경로가 아닐 때만 ChatApp 렌더링 */}
                        {/* {user && !isAdminRoute && <ChatApp />} */}
                        {children}
                    </main>
                </SemanticSidebar.Pusher>
            </SemanticSidebar.Pushable>
            <Footer />
        </div>
    );
};

export default Layout;
