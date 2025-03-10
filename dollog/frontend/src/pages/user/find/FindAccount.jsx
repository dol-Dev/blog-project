import 'bootstrap/dist/css/bootstrap.min.css';
import React, { useState } from 'react';
import { toast } from 'react-toastify';
import { Button, Icon, Loader } from 'semantic-ui-react';
import axiosInstance from '../../../utils/axiosInstance';
import styles from './findAccount.module.css';

const FindAccount = () => {
    const [isRightPanelActive, setIsRightPanelActive] = useState(false);
    const [emailForId, setEmailForId] = useState('');
    const [codeForId, setCodeForId] = useState('');
    const [emailForPassword, setEmailForPassword] = useState('');
    const [codeForPassword, setCodeForPassword] = useState('');
    const [emailSentForId, setEmailSentForId] = useState(false);
    const [emailSentForPassword, setEmailSentForPassword] = useState(false);
    const [codeVerifiedForId, setCodeVerifiedForId] = useState(false);
    const [codeVerifiedForPassword, setCodeVerifiedForPassword] = useState(false);
    const [loading, setLoading] = useState(false);

    // 아이디 찾기
    const handleSendCodeForId = async (email) => {
        setLoading(true);
        try {
            await axiosInstance.post('/api/accounts/recovery/code', null, {
                params: { email }
            });
            toast.success('인증 코드가 전송되었습니다. 이메일을 확인하고 인증해주세요.');
            setEmailSentForId(true);
        } catch (error) {
            toast.error('인증 코드 전송 실패. 다시 시도해주세요.');
        } finally {
            setLoading(false);
        }
    };

    const handleVerifyCodeForId = async (code) => {
        setLoading(true);
        try {
            await axiosInstance.post('/api/accounts/recovery/code/verify', null, {
                params: { code }
            });
            toast.success('인증이 완료되었습니다. 아래 링크를 클릭하여 아이디를 확인하세요.');
            setCodeVerifiedForId(true);
        } catch (error) {
            toast.error('인증 실패. 다시 시도해주세요.');
        } finally {
            setLoading(false);
        }
    };

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
    const handleSendCodeForPassword = async (email) => {
        setLoading(true);
        try {
            await axiosInstance.post('/api/accounts/recovery/code', null, {
                params: { email }
            });
            toast.success('인증 코드가 전송되었습니다. 이메일을 확인하고 인증해주세요.');
            setEmailSentForPassword(true);
        } catch (error) {
            toast.error('인증 코드 전송 실패. 다시 시도해주세요.');
        } finally {
            setLoading(false);
        }
    };

    const handleVerifyCodeForPassword = async (code) => {
        setLoading(true);
        try {
            await axiosInstance.post('/api/accounts/recovery/code/verify', null, {
                params: { code }
            });
            toast.success('인증이 완료되었습니다. 아래 링크를 클릭하여 비밀번호를 재설정하세요.');
            setCodeVerifiedForPassword(true);
        } catch (error) {
            toast.error('인증 실패. 다시 시도해주세요.');
        } finally {
            setLoading(false);
        }
    };

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
                                value={emailForId}
                                onChange={(e) => setEmailForId(e.target.value)}
                                disabled={emailSentForId}
                            />
                            <Button icon
                                circular
                                onClick={() => handleSendCodeForId(emailForId)}
                                disabled={emailSentForId || loading}
                                color='black'
                                className={styles['button-margin']}>
                                <Icon name='send' />
                            </Button>
                        </div>
                        {loading && <Loader active inline='centered' />}
                        {emailSentForId && (
                            <div className={styles['form-group']}>
                                <input
                                    type="text"
                                    placeholder="8자리 인증 코드를 입력하세요."
                                    value={codeForId}
                                    onChange={(e) => setCodeForId(e.target.value)}
                                    disabled={codeVerifiedForId}
                                />
                                <Button icon
                                    circular
                                    onClick={() => handleVerifyCodeForId(codeForId)}
                                    disabled={codeVerifiedForId || loading}
                                    color='black'
                                    className={styles['button-margin']}>
                                    <Icon name='check' />
                                </Button>
                            </div>
                        )}
                        {codeVerifiedForId && (
                            <div className={styles['message info']}>
                                <p>
                                    인증이 완료되었습니다. {' '}
                                    <span onClick={() => handleSendUsername(emailForId)} className={styles['link']}>
                                        여기
                                    </span>
                                    를 클릭하여 이메일로 아이디를 받으세요.
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
                                value={emailForPassword}
                                onChange={(e) => setEmailForPassword(e.target.value)}
                                disabled={emailSentForPassword}
                            />
                            <Button icon
                                circular
                                onClick={() => handleSendCodeForPassword(emailForPassword)}
                                disabled={emailSentForPassword || loading}
                                color='black'
                                className={styles['button-margin']}>
                                <Icon name='send' />
                            </Button>
                        </div>
                        {loading && <Loader active inline='centered' />}
                        {emailSentForPassword && (
                            <div className={styles['form-group']}>
                                <input
                                    type="text"
                                    placeholder="8자리 인증 코드를 입력하세요."
                                    value={codeForPassword}
                                    onChange={(e) => setCodeForPassword(e.target.value)}
                                    disabled={codeVerifiedForPassword}
                                />
                                <Button icon
                                    circular
                                    onClick={() => handleVerifyCodeForPassword(codeForPassword)}
                                    disabled={codeVerifiedForPassword || loading}
                                    color='black'
                                    className={styles['button-margin']}>
                                    <Icon name='check' />
                                </Button>
                            </div>
                        )}
                        {codeVerifiedForPassword && (
                            <div className={styles['message info']}>
                                <p>
                                    인증이 완료되었습니다. {' '}
                                    <span onClick={() => handleSendTempPassword(emailForPassword)} className={styles['link']}>
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
                                onClick={() => setIsRightPanelActive(false)}
                                id="findId"
                            >
                                아이디 찾기
                            </button>
                        </div>
                        <div className={`${styles['overlay-panel']} ${styles['overlay-right']}`}>
                            <h1>비밀번호 재설정</h1>
                            <p>이메일을 입력하여 비밀번호를 재설정하세요.</p>
                            <button
                                className={styles['ghost']}
                                onClick={() => setIsRightPanelActive(true)}
                                id="findPassword"
                            >
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