import 'bootstrap/dist/css/bootstrap.css';
import React, { useEffect, useState } from 'react';
import { Nav, Navbar } from 'react-bootstrap';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button, Dropdown, Icon } from 'semantic-ui-react';
import { useBlog } from '../../contexts/BlogContext';
import AVATAR_URL from '../../utils/avatarUrl';
import axiosInstance from '../../utils/axiosInstance';
import { useAuth } from '../../contexts/AuthContext';
import styles from './header.module.css';

const Header = ({ onBlogSidebarToggle }) => {
    const { authInfo } = useAuth();
    const { blogName, setBlogName } = useBlog();
    const navigate = useNavigate();
    const location = useLocation();
    const [nickname, setNickname] = useState('');
    const [blogNameByAuthInfo, setBlogNameByAuthInfo] = useState('');
    const [avatar, setAvatar] = useState('');

    useEffect(() => {
        updateLoginStatus();
    }, [authInfo]);

    const updateLoginStatus = () => {
        if (authInfo) {
            const { avatarImageName, nickname, blogName } = authInfo;
            setAvatar(AVATAR_URL + avatarImageName);
            setNickname(nickname);
            setBlogNameByAuthInfo(blogName);
            console.log("blogNameByAuthInfo : " + blogNameByAuthInfo);
        }
    }

    const handleLogout = async () => {
        if (!authInfo) return;
        try {
            const response = await axiosInstance.post('/api/auth/logout');

            if (response.status === 200) {
                navigate('/');
                window.location.reload();
            }
        } catch (error) {
            console.error('Error logging out:', error);
            alert('로그아웃 중 오류가 발생했습니다.');
        }
    };

    return (
        <>
            <Navbar bg="transparent" variant="light" expand="md" className={styles['custom-navbar']}>
                {blogName ? (
                    <Button icon className={styles['transparent-button']} onClick={(e) => {
                        e.preventDefault();
                        onBlogSidebarToggle();
                    }}>
                        <Icon name={"bars"} className={styles['icon']} />
                    </Button>
                ) : (
                    <></>
                )}
                <Navbar.Brand className={styles['navbar-brand']}>{blogName}</Navbar.Brand>
                <Navbar.Collapse id="collapsibleNavbar">
                    <Nav className="mr-auto" onClick={() => {
                        setBlogName('')
                    }}>
                        {authInfo ? (
                            <>
                                <Nav.Link>
                                    <Dropdown
                                        trigger={
                                            <img src={avatar} alt="Avatar" className={styles['avatar']} />
                                        }
                                        pointing="top"
                                        icon={null}
                                    >
                                        <Dropdown.Menu>
                                            <Dropdown.Item text="정보 수정" icon="info" onClick={() => navigate('/user')} />
                                            <Dropdown.Item text="블로그 관리" icon="adn" onClick={() => navigate('/manage')} />
                                            <Dropdown.Item text="로그아웃" icon="power off" onClick={handleLogout} />
                                        </Dropdown.Menu>
                                    </Dropdown>
                                </Nav.Link>
                                {blogName !== '' ? (
                                    <>
                                        <Nav.Link as={Link} to="/" onClick={() => {
                                            setBlogName('');
                                        }} className={styles['nav-link']}>블로그 홈</Nav.Link>
                                        <Nav.Link as={Link} to="/write" className={styles['nav-link']}>글쓰기</Nav.Link>
                                    </>
                                ) : (
                                    <Nav.Link as={Link} to={`/blog/${nickname}`} state={{ blogName: blogNameByAuthInfo }} className={styles['nav-link']}>내블로그</Nav.Link>
                                )}
                            </>
                        ) : (
                            <>
                                {(location.pathname === '/login' || location.pathname === '/find') && (
                                    <Nav.Link as={Link} to="/" className={styles['nav-link']}>블로그 홈</Nav.Link>
                                )}
                                <Nav.Link as={Link} to="/login" className={styles['nav-link']}>로그인</Nav.Link>
                            </>
                        )}
                    </Nav>
                </Navbar.Collapse>
            </Navbar>
        </>
    );
};

export default Header;