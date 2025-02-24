import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import Swal from 'sweetalert2';
import { useAuth } from '../../../contexts/AuthContext';
import axiosInstance from '../../../utils/axiosInstance';
import FindAccount from '../find/FindAccount';
import styles from './loginAndSignUp.module.css';

const LoginAndSignUp = () => {
    const navigate = useNavigate();

    const { fetchUserInfo } = useAuth();

    const [isRightPanelActive, setIsRightPanelActive] = useState(false);
    const [showFindModal, setShowFindModal] = useState(false);
    const [nickname, setNickname] = useState('');
    const [email, setEmail] = useState('');
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');

    // 로그인 & 회원가입 에러 상태 관리
    const [LoginErrorState, setLoginErrorState] = useState({
        username: '',
        password: '',
    });

    const [SignUpErrorState, setSignUpErrorState] = useState({
        nickname: '',
        email: '',
        username: '',
        password: ''
    });

    const handleLogin = async () => {
        // 로그인 요청 전 에러 상태 초기화
        setLoginErrorState({ username: '', password: '' });
        try {
            await axiosInstance.post('/api/auth/login', {
                username: username,
                password: password
            });

            await Swal.fire({
                icon: 'success',
                text: '환영합니다!'
            }).then(() => {
                navigate('/');
                fetchUserInfo();
            });
        } catch (error) {
            if (error.response) {
                const errors = error.response.data.errors;
                if (errors) {
                    setLoginErrorState({
                        username: errors.username || '',
                        password: errors.password || ''
                    });
                }
                toast.error('로그인 실패. 다시 시도해주세요.');
            }
        }
    };

    const handleSignup = async () => {
        // 회원가입 요청 전 에러 상태 초기화
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
                console.log(error.response.data);
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
                        {SignUpErrorState.nickname && <span className={styles['login-signup-error']}>{SignUpErrorState.nickname}</span>}

                        <input type="email" placeholder="이메일" value={email} onChange={(e) => setEmail(e.target.value)} />
                        {SignUpErrorState.email && <span className={styles['login-signup-error']}>{SignUpErrorState.email}</span>}

                        <input type="text" placeholder="아이디" value={username} onChange={(e) => setUsername(e.target.value)} />
                        {SignUpErrorState.username && <span className={styles['login-signup-error']}>{SignUpErrorState.username}</span>}

                        <input type="password" placeholder="비밀번호" value={password} onChange={(e) => setPassword(e.target.value)} />
                        {SignUpErrorState.password && <span className={styles['login-signup-error']}>{SignUpErrorState.password}</span>}

                        <br /><br />
                        <button type="button" onClick={handleSignup}>회원가입</button>
                    </form>
                </div>

                {/* 로그인 폼 */}
                <div className={`${styles['form-container']} ${styles['sign-in-container']}`}>
                    <form action="#">
                        <h1>로그인</h1>
                        <input type="text" placeholder="아이디" value={username} onChange={(e) => setUsername(e.target.value)} />
                        {LoginErrorState.username && <span className={styles['login-signup-error']}>{LoginErrorState.username}</span>}

                        <input type="password" placeholder="비밀번호" value={password} onChange={(e) => setPassword(e.target.value)} />
                        {LoginErrorState.password && <span className={styles['login-signup-error']}>{LoginErrorState.password}</span>}

                        <Link as={Link} to="/find" onClick={() => setShowFindModal(true)} style={{ margin: '30px 0 30px 0' }}>아이디/비밀번호를 잊으셨나요?</Link>
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
                            <a href={`http://localhost:8080/oauth2/authorization/google`}>
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
                            <button className={styles['ghost']} onClick={() => setIsRightPanelActive(false)}>로그인</button>
                        </div>
                        <div className={`${styles['overlay-panel']} ${styles['overlay-right']}`}>
                            <h1>처음보넹 ㅎㅇ</h1>
                            <p>회원가입 ㄱ</p>
                            <button className={styles['ghost']} onClick={() => setIsRightPanelActive(true)}>회원가입</button>
                        </div>
                    </div>
                </div>

                {showFindModal && <FindAccount onClose={() => setShowFindModal(false)} />}
            </div>
        </div>
    );
};

export default LoginAndSignUp;
