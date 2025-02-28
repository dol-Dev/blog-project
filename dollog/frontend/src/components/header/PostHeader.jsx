import 'bootstrap/dist/css/bootstrap.css';
import React, { useEffect, useState } from 'react';
import { Nav, Navbar } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { Dropdown } from 'semantic-ui-react';
import { useAuth } from '../../contexts/AuthContext';
import AVATAR_URL from '../../utils/avatarUrl';
import CustomToolbar from '../toolbar/CustomToolbar';
import styles from './postHeader.module.css';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import axiosInstance from '../../utils/axiosInstance';

const PostHeader = () => {

    const navigate = useNavigate();
    const { authInfo } = useAuth();
    const [nickname, setNickname] = useState('');
    const [avatar, setAvatar] = useState('');
    const [blogName, setBlogName] = useState('');
    

    useEffect(() => {
        updateLoginStatus();
    }, [authInfo]);

    const updateLoginStatus = () => {
        if (authInfo) {
            const { avatarImageName, nickname, blogName } = authInfo;
            setAvatar(AVATAR_URL + avatarImageName);
            setNickname(nickname);
            setBlogName(blogName);
        }
    }

    const handleLogout = async () => {
        if (!authInfo) return;
        try {
            const response = await axiosInstance.post('/api/autah/logout');

            if (response.status === 200) {
                navigate('/');
                window.location.reload();
            }
        } catch (error) {
            toast.error('로그아웃에 실패했습니다. 다시 시도해주세요.');
        }
    };

    return (
        <Navbar bg="transparent" variant="light" className={styles['custom-navbar']}>
            <Navbar.Collapse id="collapsibleNavbar">
                <Nav className={`${styles.navContainer}`}>
                    <>
                        <Nav.Link>
                            <Dropdown
                                trigger={<img src={avatar} alt="Avatar" className={styles['avatar']} />}
                                pointing="top"
                                icon={null}
                            >
                                <Dropdown.Menu>
                                    <Dropdown.Item text="프로필 정보 수정" icon="info" onClick={() => navigate('/profile')} />
                                    <Dropdown.Item text="회원 정보 수정" icon="user" onClick={() => navigate('/user')} />
                                    <Dropdown.Item text="블로그 관리" icon="adn" onClick={() => navigate('/manage')} />
                                    <Dropdown.Item text="로그아웃" icon="power off" onClick={handleLogout} />
                                </Dropdown.Menu>
                            </Dropdown>
                        </Nav.Link>
                        <Nav.Link
                            as={Link}
                            to={`/blog/${nickname}`}
                            state={{ blogName: blogName }}
                            className={styles['nav-link-blogName']}
                        >
                            내블로그
                        </Nav.Link>
                        <Nav.Link>
                            <div className={styles.customToolbarWrapper}>
                                <CustomToolbar />
                            </div>
                        </Nav.Link>
                    </>
                </Nav>
            </Navbar.Collapse>
        </Navbar>
    );
};

export default PostHeader;