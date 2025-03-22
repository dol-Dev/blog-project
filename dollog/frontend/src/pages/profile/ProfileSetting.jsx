import 'bootstrap/dist/css/bootstrap.min.css';
import React, { useEffect, useState } from 'react';
import { useNavigate } from "react-router-dom";
import { toast } from 'react-toastify';
import { useAuth } from '../../contexts/AuthContext';
import AVATAR_URL from '../../utils/avatarUrl';
import axiosInstance from '../../utils/axiosInstance';
import styles from './profileSetting.module.css'; // 모듈 CSS 임포트

const ProfileSetting = () => {
    const navigate = useNavigate();
    const [nickname, setNickname] = useState('');
    const [avatar, setAvatar] = useState('');
    const [blogName, setBlogName] = useState('');
    const [selectedAvatar, setSelectedAvatar] = useState(null);
    const { authInfo } = useAuth();

    const [errorState, setErrorState] = useState({
        nickname: '',
        blogName: '',
        avatar: ''
    });

    useEffect(() => {
        if (authInfo) {
            const { nickname, avatarImageName, blogName } = authInfo;
            setAvatar(AVATAR_URL + avatarImageName);
            setNickname(nickname);
            setBlogName(blogName);
        }
    }, [authInfo]);

    // 아바타 업데이트
    const updateAvatar = async (e) => {
        const file = e.target.files[0];
        if (file) {
            // 미리보기 설정 (Data URL)
            const reader = new FileReader();
            reader.onloadend = () => {
                setSelectedAvatar(reader.result);
            };
            reader.readAsDataURL(file);

            // FormData 객체 생성 및 파일 추가
            const formData = new FormData();
            formData.append('avatarFile', file);

            setErrorState({ ...errorState, avatar: '' });
            try {
                const response = await axiosInstance.put('/api/profiles/avatar', formData, {
                    headers: {
                        'Content-Type': 'multipart/form-data',
                    },
                });
                if (response.status === 200) {
                    // navigate('/profile');
                    toast.success("수정 성공.");
                }
            } catch (error) {
                if (error.response) {
                    const errors = error.response.data.errors || {};
                    setErrorState((prevState) => ({
                        ...prevState,
                        avatar: errors.avatarFile || ''
                    }));
                }
                toast.error("수정 실패. 다시 시도해주세요.");
            }
        }
    };

    // 닉네임 업데이트
    const updateNickname = async () => {
        setErrorState({ ...errorState, nickname: '' });
        try {
            const response = await axiosInstance.put('/api/profiles/nickname', {
                newNickname: nickname
            });
            if (response === 200) {
                navigate('/');
            }
        } catch (error) {
            if (error.response) {
                const errors = error.response.data.errors || {};
                setErrorState((prevState) => ({
                    ...prevState,
                    nickname: errors.newNickname || ''
                }));
            }

            toast.error("수정 실패. 다시 시도해주세요.");
        }
    };

    // 블로그이름 업데이트
    const updateBlogName = async () => {
        setErrorState({ ...errorState, blogName: '' });
        try {
            const response = await axiosInstance.put('/api/profiles/blogName', {
                newBlogName: blogName
            });
            if (response === 200) {
                navigate('/');
            }
        } catch (error) {
            if (error.response) {
                const errors = error.response.data.errors || {};
                setErrorState((prevState) => ({
                    ...prevState,
                    blogName: errors.newBlogName || ''
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
                    <span>프로필 편집 </span>
                </h2>
                <div className={styles.profileContainer}>
                    {/* 아바타 이미지 */}
                    <div className={styles.avatarContainer}>
                        <img
                            src={selectedAvatar || avatar}
                            className={styles.avatarImage}
                        />
                        {/* 호버 시 나타나는 오버레이 */}
                        <div
                            className={styles.avatarOverlay}
                            onClick={() => document.getElementById('avatar-input').click()}
                        >
                            아바타 수정
                        </div>
                        <input
                            id="avatar-input"
                            type="file"
                            accept="image/*"
                            style={{ display: 'none' }}
                            onChange={updateAvatar}
                        />
                        {errorState.avatar && <span className={styles.errorMessage}>{errorState.avatar}</span>}
                    </div>

                    {/* 프로필 입력 폼 */}
                    <div className={styles.profileInput}>
                        <div className={styles.inputGroup}>
                            <label>닉네임</label>
                            <input
                                type="text"
                                placeholder="닉네임을 입력하세요"
                                value={nickname}
                                onChange={(e) => setNickname(e.target.value)}
                            />
                            {errorState.nickname && <span className={styles.errorMessage}>{errorState.nickname}</span>}
                            <button type="button" onClick={updateNickname}>수정</button>
                        </div>

                        <div className={styles.inputGroup}>
                            <label>블로그 이름</label>
                            <input
                                type="text"
                                placeholder="블로그 이름을 입력하세요"
                                value={blogName}
                                onChange={(e) => setBlogName(e.target.value)}
                            />
                            {errorState.blogName && <span className={styles.errorMessage}>{errorState.blogName}</span>}
                            <button type="button" onClick={updateBlogName}>수정</button>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
};

export default ProfileSetting;
