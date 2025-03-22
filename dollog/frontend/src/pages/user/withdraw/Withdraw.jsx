import 'bootstrap/dist/css/bootstrap.min.css';
import React, { useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import Swal from 'sweetalert2';
import { useAuth } from '../../../contexts/AuthContext';
import axiosInstance from '../../../utils/axiosInstance';
import styles from './withdraw.module.css';

const Withdraw = () => {
    const [email, setEmail] = useState('');
    const [verificationCode, setVerificationCode] = useState('');
    const [emailSent, setEmailSent] = useState(false);
    const [codeVerified, setCodeVerified] = useState(false);
    const [loading, setLoading] = useState(false);
    const [provider, setProvider] = useState('');

    const [errorState, setErrorState] = useState({
        email: '',
        code: '',
    });

    const { authInfo } = useAuth();

    useEffect(() => {
        if (authInfo) {
            setProvider(authInfo.provider || '');
        }
    }, [authInfo]);


    // 일반 계정용: 이메일로 탈퇴용 인증 코드 발송
    const handleSendCode = async () => {
        setErrorState({ ...errorState, email: '' });
        if (authInfo && !(authInfo.email === email)) {
            setErrorState({ ...errorState, email: '해당 사용자의 이메일과 일치하지 않습니다.' });
        }
        setLoading(true);
        try {
            await axiosInstance.post('/api/codes', {
                email: email,
            });
            toast.success('인증 코드가 전송되었습니다. 이메일을 확인해주세요.');
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

    // 일반 계정용: 입력한 인증 코드 확인
    const handleVerifyCode = async () => {
        setErrorState({ ...errorState, code: '' });
        setLoading(true);
        try {
            await axiosInstance.post('/api/codes/verify', {
                code: verificationCode,
            });
            toast.success('인증이 완료되었습니다.');
            setCodeVerified(true);
        } catch (error) {
            if (error.response) {
                const errors = error.response.data.errors || {};
                setErrorState((prevState) => ({
                    ...prevState,
                    code: errors.code || ''
                }));
            }
            toast.error('코드 인증 실패. 다시 시도해주세요.');
        } finally {
            setLoading(false);
        }
    };

    const handleWithdraw = async () => {
        Swal.fire({
            title: '<strong>회원 탈퇴 진행</strong>',
            html: `
            <div style="font-size:16px; color:#555; line-height:1.5;">
              <p>회원 탈퇴 유예기간 7일 동안 계정이 비활성화되며,</p>
              <p>7일이 지나면 영구적으로 탈퇴가 진행됩니다.</p>
              <p>진행하시려면 아래에 <strong>"회원탈퇴"</strong>를 입력해주세요.</p>
              <input id="swal-input" class="swal2-input" placeholder="회원탈퇴" style="font-size:16px; padding:8px;">
            </div>
          `,
            showCancelButton: true,
            confirmButtonText: '네, 진행합니다',
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
                    confirmButton.disabled = input.value !== '회원탈퇴';
                });
            },
            preConfirm: () => {
                return document.getElementById('swal-input').value;
            }
        }).then(async (result) => {
            if (result.isConfirmed) {
                setLoading(true);
                try {
                    await axiosInstance.delete('/api/auth/withdraw');
                    Swal.fire({
                        title: '<strong>탈퇴 완료</strong>',
                        html: '회원 탈퇴 요청이 접수되었습니다.<br>7일 이후 탈퇴가 진행됩니다.',
                        icon: 'success',
                        confirmButtonText: '확인',
                        background: '#fff',
                        customClass: {
                            popup: 'my-swal-popup',
                            confirmButton: 'my-swal-confirm'
                        },
                        confirmButtonColor: '#3085d6'
                    }).then((res) => {
                        if (res.isConfirmed) {
                            window.location.href = '/';
                        }
                    });
                } catch (error) {
                    toast.error('회원 탈퇴 요청 실패. 다시 시도해주세요.');
                } finally {
                    setLoading(false);
                }
            }
        });
    };


    // SNS 계정
    if (provider) {
        return (
            <>
                <br /><br />
                <div className={`container ${styles.containerStyle}`}>
                    <h2 className={styles.title}>
                        <span>회원 탈퇴</span>
                    </h2>
                    <div className={styles.userContainer}>
                        <div className={styles.userInput}>
                            <p>SNS 계정은 인증 과정 없이 바로 회원 탈퇴 신청이 가능합니다.</p>
                            <div className={styles.inputGroup}>
                                <button
                                    type="button"
                                    onClick={handleWithdraw}
                                    disabled={loading}
                                    className={styles.withdrawButton}
                                >
                                    {loading ? '로딩중...' : '회원 탈퇴 신청'}
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </>
        );
    }

    // 일반 계정
    return (
        <>
            <br /><br />
            <div className={`container ${styles.containerStyle}`}>
                <h2 className={styles.title}>
                    <span>회원 탈퇴</span>
                </h2>
                <div className={styles.userContainer}>
                    <div className={styles.userInput}>
                        <form>
                            <div className={styles.inputGroup}>
                                <input
                                    type="email"
                                    placeholder="이메일을 입력하여 인증 코드를 받으세요."
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    disabled={emailSent}
                                />
                                <button
                                    type="button"
                                    onClick={handleSendCode}
                                    disabled={!email || emailSent || loading}
                                    className={styles.buttonMargin}
                                >
                                    {loading ? '로딩중...' : '인증 코드 발송'}
                                </button>
                                {errorState.email && <span className={styles.errorMessage}>{errorState.email}</span>}
                            </div>


                            {emailSent && (
                                <div className={styles.inputGroup}>
                                    <input
                                        type="text"
                                        placeholder="8자리 인증 코드를 입력하세요."
                                        value={verificationCode}
                                        onChange={(e) => setVerificationCode(e.target.value)}
                                        disabled={codeVerified}
                                    />
                                    <button
                                        type="button"
                                        onClick={handleVerifyCode}
                                        disabled={!verificationCode || codeVerified || loading}
                                        className={styles.buttonMargin}
                                    >
                                        {loading ? '로딩중...' : '인증'}
                                    </button>
                                    {errorState.code && <span className={styles.errorMessage}>{errorState.code}</span>}
                                </div>
                            )}

                            {codeVerified && (
                                <div className={styles.inputGroup}>
                                    <button
                                        type="button"
                                        onClick={handleWithdraw}
                                        disabled={loading}
                                        className={styles.withdrawButton}
                                    >
                                        회원 탈퇴 신청
                                    </button>
                                </div>
                            )}
                        </form>
                    </div>
                </div>
            </div>
        </>
    );
};

export default Withdraw;
