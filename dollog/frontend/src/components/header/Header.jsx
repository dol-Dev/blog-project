import React, { useEffect, useState } from 'react';
import { Nav, Navbar } from 'react-bootstrap';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { Button, Dropdown, Icon, Input } from 'semantic-ui-react';
import { useAuth } from '../../contexts/AuthContext';
import { useBlog } from '../../contexts/BlogContext';
import AVATAR_URL from '../../utils/avatarUrl';
import axiosInstance from '../../utils/axiosInstance';
import styles from './header.module.css';

const Header = ({ onBlogSidebarToggle }) => {
    const { authInfo } = useAuth();
    const { blogName, setBlogName, setNickname, setProvider } = useBlog();
    const navigate = useNavigate();
    const location = useLocation();
    const [blogNameByAuthInfo, setBlogNameByAuthInfo] = useState({ blogName: '', provider: '' });
    const [avatar, setAvatar] = useState('');

    // 검색 관련 상태
    const [searchKeyword, setSearchKeyword] = useState('');
    const [searchType, setSearchType] = useState('0');
    const [showSearch, setShowSearch] = useState(true);

    useEffect(() => {
        updateLoginStatus();
    }, [authInfo]);

    const updateLoginStatus = () => {
        if (authInfo) {
            const { avatarImageName, nickname, blogName, provider } = authInfo;
            setAvatar(AVATAR_URL + avatarImageName);
            setBlogNameByAuthInfo({ blogName, provider });
        }
    };

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

    // 검색 요청 처리
    const handleSearchSubmit = (e) => {
        e.preventDefault();
        if (!showSearch) return;

        const queryParams = {
            type: searchType,
            keyword: searchKeyword,
        };

        const queryString = Object.entries(queryParams)
            .map(([key, value]) => `${key}=${encodeURIComponent(value)}`)
            .join('&');

        navigate(`/search-post?${queryString}`);
    };

    const handleKeyDown = (e) => {
        if (e.key === 'Enter') {
            handleSearchSubmit(e);
        }
    };

    // 검색창 노출 여부
    const shouldShowSearch =
        location.pathname === '/' ||
        location.pathname.startsWith('/posts') ||
        location.pathname.startsWith('/search-post');

    return (
        <Navbar bg="transparent" variant="light" className={styles['custom-navbar']}>
            <div className={styles.headerContainer}>
                {/* 왼쪽 섹션: 메뉴 */}
                <div className={styles.leftSection}>
                    <Nav>
                        {authInfo ? (
                            <>
                                {blogName !== '' ? (
                                    <>
                                        <Nav.Link
                                            onClick={(e) => {
                                                sessionStorage.removeItem('nickname');
                                                sessionStorage.removeItem('provider');
                                                sessionStorage.removeItem('blogName');
                                                setBlogName('');
                                                setNickname('');
                                                setProvider('');
                                                navigate('/');
                                            }}
                                            className={styles['nav-link']}
                                        >
                                            블로그 홈
                                        </Nav.Link>
                                        <Nav.Link as={Link} to="/write-post" className={styles['nav-link']}>
                                            글쓰기
                                        </Nav.Link>
                                    </>
                                ) : (
                                    <Nav.Link
                                        as={Link}
                                        to={`/blog/${authInfo?.nickname}`}
                                        className={styles['nav-link']}
                                        state={{
                                            blogName: blogNameByAuthInfo.blogName,
                                            provider: blogNameByAuthInfo.provider,
                                        }}
                                    >
                                        내블로그
                                    </Nav.Link>
                                )}
                            </>
                        ) : (
                            <>
                                <Nav.Link as={Link} to="/" className={styles['nav-link']}>
                                    블로그 홈
                                </Nav.Link>
                                <Nav.Link as={Link} to="/login" className={styles['nav-link']}>
                                    로그인
                                </Nav.Link>
                            </>
                        )}
                    </Nav>
                </div>

                {/* 가운데 섹션: 검색창 (항상 컨테이너는 렌더링) */}
                <div className={styles.centerSection}>
                    {shouldShowSearch && (
                        <Nav>
                            <>
                                <Dropdown
                                    selection
                                    value={searchType}
                                    options={[
                                        { key: 'title', text: '제목', value: '0' },
                                        { key: 'content', text: '내용', value: '1' },
                                        { key: 'titleAndContent', text: '제목+내용', value: '2' },
                                    ]}
                                    onChange={(e, { value }) => setSearchType(value)}
                                    defaultValue="0"
                                    text={
                                        searchType === '0' ? '제목' : searchType === '1' ? '내용' : '제목+내용'
                                    }
                                    style={{ minWidth: '105px', marginRight: '10px', borderRadius: '5px' }}
                                />
                                <Input
                                    className="prompt"
                                    type="text"
                                    id="searchKeyword"
                                    placeholder="검색어를 입력하세요"
                                    value={searchKeyword}
                                    onChange={(e) => setSearchKeyword(e.target.value)}
                                    onKeyDown={handleKeyDown}
                                    style={{ minWidth: '200px', paddingRight: '4px', textAlign: 'center' }}
                                />
                                <Button
                                    type="button"
                                    color="black"
                                    style={{
                                        border: 'none',
                                        background: 'transparent',
                                        cursor: 'pointer',
                                        marginRight: '10px',
                                    }}
                                    onClick={(e) => {
                                        handleSearchSubmit(e);
                                    }}
                                >
                                    <Icon name="search" style={{ color: 'black' }} />
                                </Button>
                            </>
                        </Nav>
                    )}
                </div>

                {/* 오른쪽 섹션: 아바타 */}
                <div className={styles.rightSection}>
                    <Dropdown
                        trigger={<img src={avatar} alt="Avatar" className={styles['avatar']} />}
                        pointing="top"
                        icon={null}
                        direction="left"
                    >
                        <Dropdown.Menu>
                            <Dropdown.Item text="프로필 정보 수정" icon="info" onClick={() => navigate('/profile')} />
                            {!blogNameByAuthInfo.provider && (
                                <Dropdown.Item text="회원 정보 수정" icon="user" onClick={() => navigate('/user')} />
                            )}
                            <Dropdown.Item text="블로그 관리" icon="adn" onClick={() => navigate('/manage/dashboard')} />
                            <Dropdown.Item text="로그아웃" icon="power off" onClick={handleLogout} />
                        </Dropdown.Menu>
                    </Dropdown>
                </div>
            </div>
        </Navbar>
    );
};

export default Header;
