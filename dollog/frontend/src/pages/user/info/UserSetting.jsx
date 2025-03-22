import 'bootstrap/dist/css/bootstrap.min.css';
import React, { useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import { useAuth } from '../../../contexts/AuthContext';
import styles from './userSetting.module.css'; // 모듈 CSS 임포트
import axiosInstance from '../../../utils/axiosInstance';

const UserSetting = () => {
    const [username, setUsername] = useState('');
    const [newEmail, setNewEmail] = useState('');
    const [currentPassword, setCurrentPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmNewPassword, setConfirmNewPassword] = useState(''); // 새 비밀번호 확인
    const { authInfo } = useAuth();

    const [errorState, setErrorState] = useState({
        newEmail: '',
        currentPassword: '',
        newPassword: ''
    });

    useEffect(() => {
        if (authInfo) {
            setNewEmail(authInfo.email)
            setUsername(authInfo.username)

        }
    }, [authInfo]);


    // 이메일 업데이트
    const updateEmail = async () => {
        setErrorState({ ...errorState, newEmail: '' });
        try {
            const response = await axiosInstance.put('/api/users/email', {
                newEmail: newEmail
            });
            if (response === 200) {
                // navigate('/');
            }
        } catch (error) {
            if (error.response) {
                const errors = error.response.data.errors || {};
                setErrorState((prevState) => ({
                    ...prevState,
                    newEmail: errors.newEmail || ''
                }));
            }
            toast.error("수정 실패. 다시 시도해주세요.");
        }
    };

    // 비밀번호 업데이트
    const updatePassword = async () => {
        setErrorState({ ...errorState, currentPassword: '', newPassword: '' });
        try {
            if (newPassword && newPassword !== confirmNewPassword) {
                setErrorState({ ...errorState, newPassword: '새 비밀번호가 일치하지 않습니다.' });
                toast.error("수정 실패. 다시 시도해주세요.");
                return;
            }
            const response = await axiosInstance.put('/api/users/password', {
                currentPassword: currentPassword,
                newPassword: newPassword,
                username: username
            });
            if (response === 200) {
                // navigate('/');
            }
        } catch (error) {
            if (error.response) {
                const errors = error.response.data.errors || {};
                setErrorState((prevState) => ({
                    ...prevState,
                    currentPassword: errors.currentPassword || '',
                    newPassword: errors.newPassword || ''
                }));
            }
            toast.error("수정 실패. 다시 시도해주세요.");
        }
    };

    return (
        <>
            <br /><br />
            <div className={`container ${styles.containerStyle}`}>
                <h2 className={styles.title}>
                    <span>회원 정보 수정 </span>
                </h2>
                <div className={styles.userContainer}>
                    <div className={styles.userInput}>
                        <div className={styles.inputGroup}>
                            <label>아이디</label>
                            <input
                                type="text"
                                value={username}
                                className="form-control"
                                readOnly
                            />
                        </div>

                        <div className={styles.inputGroup}>
                            <label>이메일</label>
                            <input
                                type="text"
                                placeholder="닉네임을 입력하세요"
                                value={newEmail}
                                onChange={(e) => setNewEmail(e.target.value)}
                            />
                            {errorState.newEmail && <span className={styles.errorMessage}>{errorState.newEmail}</span>}
                            <button type="button" onClick={updateEmail}>수정</button>
                        </div>

                        <div className={styles.inputGroup}>
                            <label>현재 비밀번호</label>
                            <input
                                type="password"
                                placeholder="비밀번호를 바꾸실려면 현재 비밀번호를 입력하세요"
                                value={currentPassword}
                                onChange={(e) => setCurrentPassword(e.target.value)}
                            />
                            {errorState.currentPassword && <span className={styles.errorMessage}>{errorState.currentPassword}</span>}
                        </div>

                        {currentPassword && (
                            <div className={styles.inputGroup}>
                                <label>새 비밀번호</label>
                                <input
                                    type="password"
                                    placeholder="새 비밀번호를 입력하세요"
                                    value={newPassword}
                                    onChange={(e) => setNewPassword(e.target.value)}
                                />
                                {errorState.newPassword && <span className={styles.errorMessage}>{errorState.newPassword}</span>}
                            </div>
                        )}

                        {newPassword && (
                            <div className={styles.inputGroup}>
                                <label>새 비밀번호 확인</label>
                                <input
                                    type="password"
                                    placeholder="변경할 비밀번호를 한번 더 입력하세요"
                                    value={confirmNewPassword}
                                    onChange={(e) => setConfirmNewPassword(e.target.value)}
                                />
                                <button type="button" onClick={updatePassword}>수정</button>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </>
    );
};

export default UserSetting;
