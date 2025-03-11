import 'bootstrap/dist/css/bootstrap.css';
import React, { useEffect, useState } from 'react';
import { Nav, Navbar } from 'react-bootstrap';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { Button, Dropdown, Icon } from 'semantic-ui-react';
import { useAuth } from '../../contexts/AuthContext';
import { useBlog } from '../../contexts/BlogContext';
import AVATAR_URL from '../../utils/avatarUrl';
import axiosInstance from '../../utils/axiosInstance';
import styles from './header.module.css';

const Header = ({ onBlogSidebarToggle }) => {
    const { authInfo } = useAuth();
    const { blogName, setBlogName } = useBlog();
    const navigate = useNavigate();
    const [nickname, setNickname] = useState('');
    const [blogNameByAuthInfo, setBlogNameByAuthInfo] = useState({ blogName: '', provider: '' });

    const [avatar, setAvatar] = useState('');

    useEffect(() => {
        updateLoginStatus();
    }, [authInfo]);

    const updateLoginStatus = () => {
        if (authInfo) {
            const { avatarImageName, nickname, blogName, provider } = authInfo;
            setAvatar(AVATAR_URL + avatarImageName);
            setNickname(nickname);
            setBlogNameByAuthInfo({ blogName, provider });
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
            toast.error('로그아웃에 실패했습니다. 다시 시도해주세요.');
        }
    };

    return (
        <>
            <Navbar bg="transparent" variant="light" expand="md" className={styles['custom-navbar']}>
                {authInfo && blogName ? (
                    <Button icon className={styles['transparent-button']} onClick={(e) => {
                        e.preventDefault();
                        onBlogSidebarToggle();
                    }}>
                        <Icon name={"bars"} className={styles['icon']} />
                    </Button>
                ) : (
                    <></>
                )}
                {authInfo ? (
                    <Navbar.Brand className={styles['navbar-brand']}>{blogName}</Navbar.Brand>
                ) : (
                    <></>
                )}
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
                                            <Dropdown.Item text="프로필 정보 수정" icon="info" onClick={() => navigate('/profile')} />
                                            {!blogNameByAuthInfo.provider && (
                                                <Dropdown.Item text="회원 정보 수정" icon="user" onClick={() => navigate('/user')} />
                                            )}
                                            <Dropdown.Item text="블로그 관리" icon="adn" onClick={() => navigate('/manage')} />
                                            <Dropdown.Item text="로그아웃" icon="power off" onClick={handleLogout} />
                                        </Dropdown.Menu>
                                    </Dropdown>
                                </Nav.Link>
                                {blogName !== '' ? (
                                    <>
                                        <Nav.Link as="a" href="/" onClick={(e) => {
                                                e.preventDefault();
                                                setBlogName('');
                                                window.location.href = '/';
                                            }} className={styles['nav-link']}>블로그 홈</Nav.Link>
                                        <Nav.Link as={Link} to="/write" className={styles['nav-link']}>글쓰기</Nav.Link>
                                    </>
                                ) : (
                                    <Nav.Link as={Link} to={`/blog/${nickname}`} className={styles['nav-link']}
                                        state={{
                                            blogName: blogNameByAuthInfo.blogName,
                                            provider: blogNameByAuthInfo.provider
                                        }}
                                    >내블로그
                                    </Nav.Link>
                                )}
                            </>
                        ) : (
                            <>
                                {(!authInfo) && (
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