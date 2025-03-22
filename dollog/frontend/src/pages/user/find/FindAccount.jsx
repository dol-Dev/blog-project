import 'bootstrap/dist/css/bootstrap.min.css';
import React, { useState } from 'react';
import { toast } from 'react-toastify';
import { Button, Icon, Loader } from 'semantic-ui-react';
import axiosInstance from '../../../utils/axiosInstance';
import styles from './findAccount.module.css';

const FindAccount = () => {
    const [isRightPanelActive, setIsRightPanelActive] = useState(false);
    const [email, setEmail] = useState('');
    const [code, setCode] = useState('');
    const [emailSent, setEmailSent] = useState(false);
    const [codeVerified, setCodeVerified] = useState(false);
    const [loading, setLoading] = useState(false);

    const [errorState, setErrorState] = useState({
        email: '',
        code: '',
    });

    // 코드 전송
    const handleSendCode = async (email) => {
        setErrorState({ ...errorState, email: '' });
        setLoading(true);
        try {
            await axiosInstance.post('/api/codes', {
                email: email,
            });
            toast.success('인증 코드가 전송되었습니다. 이메일을 확인하고 인증해주세요.');
            setEmailSent(true);
        } catch (error) {
            if (error.response) {
                const errors = error.response.data.errors || {};
                setErrorState((prevState) => ({
                    ...prevState,
                    email: errors.email || ''
                }));
            }
            toast.error('인증 코드 전송 실패. 다시 시도해주세요.');
        } finally {
            setLoading(false);
        }
    };

    // 코드 검증
    const handleVerifyCode = async (verificationCode) => {
        setErrorState({ ...errorState, code: '' });
        setLoading(true);
        try {
            await axiosInstance.post('/api/codes/verify', {
                code: verificationCode,
            });
            toast.success('인증이 완료되었습니다. 아래 링크를 클릭하여 아이디를 확인하세요.');
            setCodeVerified(true);
        } catch (error) {
            if (error.response) {
                const errors = error.response.data.errors || {};
                setErrorState((prevState) => ({
                    ...prevState,
                    code: errors.code || ''
                }));
            }
            toast.error('인증 실패. 다시 시도해주세요.');
        } finally {
            setLoading(false);
        }
    };

    // 아이디 찾기
    const handleSendUsername = async (email) => {
        setLoading(true);
        try {
            await axiosInstance.post('/api/accounts/recovery/username', null, {
                params: { email }
            });
            toast.success('아이디가 이메일로 전송되었습니다.');
        } catch (error) {
            toast.error('아이디 전송 실패. 다시 시도해주세요.');
        } finally {
            setLoading(false);
        }
    };

    // 비밀번호 찾기 
    const handleSendTempPassword = async (email) => {
        setLoading(true);
        try {
            await axiosInstance.post('/api/accounts/recovery/password', null, {
                params: { email }
            });
            toast.success('임시 비밀번호가 이메일로 전송되었습니다.');
        } catch (error) {
            toast.error('임시 비밀번호 전송 실패. 다시 시도해주세요.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className={styles['find-wrapper']}>
            <div className={`${styles['find-container']} ${isRightPanelActive ? styles['right-panel-active'] : ''}`} id="findContainer">
                <div className={`${styles['form-container']} ${styles['find-id-container']}`}>
                    <form action="#">
                        <h1>아이디 찾기</h1>
                        <br />
                        <div className={styles['form-group']}>
                            <input
                                type="email"
                                placeholder="이메일을 입력하여 인증 코드를 받으세요."
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                disabled={emailSent}
                            />
                            <Button icon
                                circular
                                onClick={() => handleSendCode(email)}
                                disabled={emailSent || loading}
                                color='black'
                                className={styles['button-margin']}>
                                <Icon name='send' />
                            </Button>
                            <br/>
                            {errorState.email && <span className={styles.errorMessage}>{errorState.email}</span>}
                        </div>
                        {loading && <Loader active inline='centered' />}
                        {emailSent && (
                            <div className={styles['form-group']}>
                                <input
                                    type="text"
                                    placeholder="8자리 인증 코드를 입력하세요."
                                    value={code}
                                    onChange={(e) => setCode(e.target.value)}
                                    disabled={codeVerified}
                                />
                                <Button icon
                                    circular
                                    onClick={() => handleVerifyCode(code)}
                                    disabled={codeVerified || loading}
                                    color='black'
                                    className={styles['button-margin']}>
                                    <Icon name='check' />
                                </Button>
                                <br/>
                                {errorState.code && <span className={styles.errorMessage}>{errorState.code}</span>}
                            </div>
                        )}
                        {codeVerified && (
                            <div className={styles['message info']}>
                                <p>
                                    인증이 완료되었습니다. {' '}
                                    <span onClick={() => handleSendUsername(email)} className={styles['link']}>
                                        여기
                                    </span>
                                    를 클릭하여 이메일로 아이디를 확인하세요.
                                </p>
                            </div>
                        )}
                    </form>
                </div>
                <div className={`${styles['form-container']} ${styles['find-password-container']}`}>
                    <form action="#">
                        <h1>비밀번호 재설정</h1>
                        <br />
                        <div className={styles['form-group']}>
                            <input
                                type="email"
                                placeholder="이메일을 입력하여 인증 코드를 받으세요."
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                disabled={emailSent}
                            />
                            <Button icon
                                circular
                                onClick={() => handleSendCode(email)}
                                disabled={emailSent || loading}
                                color='black'
                                className={styles['button-margin']}>
                                <Icon name='send' />
                            </Button>
                            <br/>
                            {errorState.email && <span className={styles.errorMessage}>{errorState.email}</span>}
                        </div>
                        {loading && <Loader active inline='centered' />}
                        {emailSent && (
                            <div className={styles['form-group']}>
                                <input
                                    type="text"
                                    placeholder="8자리 인증 코드를 입력하세요."
                                    value={code}
                                    onChange={(e) => setCode(e.target.value)}
                                    disabled={codeVerified}
                                />
                                <Button icon
                                    circular
                                    onClick={() => handleVerifyCode(code)}
                                    disabled={codeVerified || loading}
                                    color='black'
                                    className={styles['button-margin']}>
                                    <Icon name='check' />
                                </Button>
                                <br/>
                                {errorState.code && <span className={styles.errorMessage}>{errorState.code}</span>}
                            </div>
                        )}
                        {codeVerified && (
                            <div className={styles['message info']}>
                                <p>
                                    인증이 완료되었습니다. {' '}
                                    <span onClick={() => handleSendTempPassword(email)} className={styles['link']}>
                                        여기
                                    </span>
                                    를 클릭하여 이메일로 임시 비밀번호를 받으세요.
                                </p>
                            </div>
                        )}
                    </form>
                </div>
                <div className={styles['overlay-container']}>
                    <div className={styles['find-overlay']}>
                        <div className={`${styles['overlay-panel']} ${styles['overlay-left']}`}>
                            <h1>아이디 찾기</h1>
                            <p>이메일을 입력하여 아이디를 찾으세요.</p>
                            <button
                                className={styles['ghost']}
                                onClick={() => {
                                    setIsRightPanelActive(false);
                                    setEmail('');
                                    setCode('');
                                    setEmailSent(false);
                                    setCodeVerified(false);
                                }}
                                id="findId">
                                아이디 찾기
                            </button>
                        </div>
                        <div className={`${styles['overlay-panel']} ${styles['overlay-right']}`}>
                            <h1>비밀번호 재설정</h1>
                            <p>이메일을 입력하여 비밀번호를 재설정하세요.</p>
                            <button
                                className={styles['ghost']}
                                onClick={() => {
                                    setIsRightPanelActive(true);
                                    setEmail('');
                                    setCode('');
                                    setEmailSent(false);
                                    setCodeVerified(false);
                                }}
                                id="findPassword">
                                비밀번호 재설정
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default FindAccount;