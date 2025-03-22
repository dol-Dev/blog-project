import React, { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import Swal from 'sweetalert2';
import { useAuth } from '../../../contexts/AuthContext';
import axiosInstance from '../../../utils/axiosInstance';
import FindAccount from '../find/FindAccount';
import styles from './loginAndSignUp.module.css';

const LoginAndSignUp = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const { fetchUserInfo } = useAuth();

    const [isRightPanelActive, setIsRightPanelActive] = useState(false);
    const [showFindModal, setShowFindModal] = useState(false);
    const [nickname, setNickname] = useState('');
    const [email, setEmail] = useState('');
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');

    const [loginErrorState, setLoginErrorState] = useState({
        username: '',
        password: '',
    });
    const [signUpErrorState, setSignUpErrorState] = useState({
        nickname: '',
        email: '',
        username: '',
        password: ''
    });

    // 계정 해제 모달 
    const showUnlockModal = async (usernameParam) => {
        const result = await Swal.fire({
            title: '<strong>비활성화 상태 해제</strong>',
            html: `
        <div style="font-size:16px; color:#555; line-height:1.5;">
          <p>현재 계정이 탈퇴 요청으로 인해 비활성화 상태입니다.</p>
          <p>해제를 원하시면 아래에 <strong>"해제"</strong>를 입력해주세요.</p>
          <input id="swal-input" class="swal2-input" placeholder="해제를 입력하세요" style="font-size:16px; padding:8px;">
        </div>
      `,
            showCancelButton: true,
            confirmButtonText: '해제합니다',
            cancelButtonText: '취소',
            focusConfirm: false,
            background: '#fff',
            confirmButtonColor: '#3085d6',
            cancelButtonColor: '#aaa',
            customClass: {
                popup: 'my-swal-popup',
                confirmButton: 'my-swal-confirm',
                cancelButton: 'my-swal-cancel'
            },
            didOpen: () => {
                const confirmButton = Swal.getConfirmButton();
                confirmButton.disabled = true;
                const input = document.getElementById('swal-input');
                input.addEventListener('input', () => {
                    confirmButton.disabled = input.value !== '해제';
                });
            },
            preConfirm: () => {
                return document.getElementById('swal-input').value;
            },
        });
        if (result.isConfirmed) {
            try {
                await axiosInstance.post('/api/auth/unlock', { username: usernameParam });
                await Swal.fire({
                    title: '<strong>해제 완료</strong>',
                    html: '계정 비활성화가 해제되었습니다.<br>다시 로그인하여 이용해 주세요.',
                    icon: 'success',
                    confirmButtonText: '확인',
                    background: '#fff',
                    confirmButtonColor: '#3085d6',
                    customClass: {
                        popup: 'my-swal-popup',
                        confirmButton: 'my-swal-confirm'
                    },
                });
                navigate('/login');
                fetchUserInfo();
            } catch (unlockError) {
                toast.error('계정 해제 실패. 다시 시도해주세요.');
            }
        }
    };

    useEffect(() => {
        const params = new URLSearchParams(location.search);
        const error = params.get('error');
        const blockedUsername = params.get('username');
        if (error === 'ACCOUNT_DISABLED' && blockedUsername) {
            showUnlockModal(blockedUsername);
        }
    }, [location]);

    const handleLogin = async () => {
        setLoginErrorState({ username: '', password: '' });
        try {
            await axiosInstance.post('/api/auth/login', {
                username: username,
                password: password,
            });

            Swal.fire({
                icon: 'success',
                title: '<strong>환영합니다!</strong>',
                text: '로그인에 성공하였습니다.',
                confirmButtonText: '확인',
                background: '#fff',
                confirmButtonColor: '#3085d6',
                customClass: {
                    popup: 'my-swal-popup',
                    confirmButton: 'my-swal-confirm'
                },
            }).then((result) => {
                if (result.isConfirmed) {
                    navigate('/');
                    fetchUserInfo();
                }
            });
        } catch (error) {
            if (error.response) {
                if (error.response.data.message === 'ACCOUNT_DISABLED') {
                    showUnlockModal(username);
                } else {
                    const errors = error.response.data.errors;
                    if (errors) {
                        setLoginErrorState({
                            username: errors.username || '',
                            password: errors.password || '',
                        });
                    }
                    toast.error('로그인 실패. 다시 시도해주세요.');
                }
            }
        }
    };

    const handleSignup = async () => {
        setSignUpErrorState({ nickname: '', email: '', username: '', password: '' });
        try {
            await axiosInstance.post('/api/users/signup', {
                username: username,
                password: password,
                email: email,
                nickname: nickname,
            });

            Swal.fire({
                icon: 'success',
                text: '회원가입이 성공적으로 완료되었습니다!'
            }).then(() => {
                navigate('/');
            });
        } catch (error) {
            if (error.response) {
                const errors = error.response.data.errors;
                if (errors) {
                    setSignUpErrorState({
                        nickname: errors.nickname || '',
                        email: errors.email || '',
                        username: errors.username || '',
                        password: errors.password || ''
                    });
                }
            }
            toast.error('회원가입 실패. 다시 시도해주세요.');
        }
    };

    return (
        <div className={styles['login-signup-wrapper']}>
            <div className={`${styles['login-signup-container']} ${isRightPanelActive ? styles['right-panel-active'] : ''}`} id="loginSignupContainer">
                {/* 회원가입 폼 */}
                <div className={`${styles['form-container']} ${styles['sign-up-container']}`}>
                    <form action="#">
                        <h1>계정 생성</h1>
                        <br />
                        <input type="text" placeholder="닉네임" value={nickname} onChange={(e) => setNickname(e.target.value)} />
                        {signUpErrorState.nickname && <span className={styles['login-signup-error']}>{signUpErrorState.nickname}</span>}

                        <input type="email" placeholder="이메일" value={email} onChange={(e) => setEmail(e.target.value)} />
                        {signUpErrorState.email && <span className={styles['login-signup-error']}>{signUpErrorState.email}</span>}

                        <input type="text" placeholder="아이디" value={username} onChange={(e) => setUsername(e.target.value)} />
                        {signUpErrorState.username && <span className={styles['login-signup-error']}>{signUpErrorState.username}</span>}

                        <input type="password" placeholder="비밀번호" value={password} onChange={(e) => setPassword(e.target.value)} />
                        {signUpErrorState.password && <span className={styles['login-signup-error']}>{signUpErrorState.password}</span>}

                        <br /><br />
                        <button type="button" onClick={handleSignup}>회원가입</button>
                    </form>
                </div>

                {/* 로그인 폼 */}
                <div className={`${styles['form-container']} ${styles['sign-in-container']}`}>
                    <form action="#">
                        <h1>로그인</h1>
                        <input type="text" placeholder="아이디" value={username} onChange={(e) => setUsername(e.target.value)} />
                        {loginErrorState.username && <span className={styles['login-signup-error']}>{loginErrorState.username}</span>}

                        <input type="password" placeholder="비밀번호" value={password} onChange={(e) => setPassword(e.target.value)} />
                        {loginErrorState.password && <span className={styles['login-signup-error']}>{loginErrorState.password}</span>}

                        <Link to="/find" onClick={() => setShowFindModal(true)} style={{ margin: '30px 0' }}>
                            아이디/비밀번호를 잊으셨나요?
                        </Link>
                        <button type="button" onClick={handleLogin}>로그인</button>

                        <div className={styles['login-signup-separator']}>
                            <p>OR</p>
                        </div>

                        <div className={styles['login-signup-social-buttons']}>
                            <a href="https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=ee8abaaf81bcb4e83dff921f9a492de6&redirect_uri=http://localhost:8080/api/oauth2/kakao/callback">
                                <img src="/images/kakao_login_button.png" className={styles['login-signup-social-button']} alt="카카오 로그인" />
                            </a>
                            <a href="https://nid.naver.com/oauth2.0/authorize?&response_type=code&client_id=INlvRIKGwlO9MzaRzyrE&redirect_uri=http://localhost:8080/api/oauth2/naver/callback">
                                <img src="/images/naver_login_button.png" className={styles['login-signup-social-button']} alt="네이버 로그인" />
                            </a>
                            <a href="http://localhost:8080/oauth2/authorization/google">
                                <div className={styles['login-signup-google-login-wrapper']}>
                                    <img src="/images/google_login_button.png" className={`${styles['login-signup-social-button']} ${styles['login-signup-google-login-button']}`} alt="구글 로그인" />
                                </div>
                            </a>
                        </div>
                    </form>
                </div>

                <div className={styles['overlay-container']}>
                    <div className={styles['overlay']}>
                        <div className={`${styles['overlay-panel']} ${styles['overlay-left']}`}>
                            <h1>안농</h1>
                            <p>로그인 해조.</p>
                            <button className={styles['ghost']} onClick={() => {
                                setIsRightPanelActive(false);
                                setNickname('');
                                setEmail('');
                                setUsername('');
                                setPassword('');
                            }}>로그인</button>
                        </div>
                        <div className={`${styles['overlay-panel']} ${styles['overlay-right']}`}>
                            <h1>처음보넹 ㅎㅇ</h1>
                            <p>회원가입 ㄱ</p>
                            <button className={styles['ghost']} onClick={() => {
                                setIsRightPanelActive(true)
                                setNickname('');
                                setEmail('');
                                setUsername('');
                                setPassword('');
                            }}>회원가입</button>
                        </div>
                    </div>
                </div>

                {showFindModal && <FindAccount onClose={() => setShowFindModal(false)} />}
            </div>
        </div>
    );
};

export default LoginAndSignUp;
