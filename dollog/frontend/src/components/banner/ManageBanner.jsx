import React, { useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import axiosInstance from '../../utils/axiosInstance';
import BANNER_URL from '../../utils/bannerUrl';
import styles from './manageBanner.module.css';

const ManageBanner = () => {
    const [bannerDescription, setBannerDescription] = useState('');
    const [selectedBanner, setSelectedBanner] = useState(null);
    const [bannerImage, setBannerImage] = useState(null);

    useEffect(() => {
        fetchBanner();
    }, []);

    const fetchBanner = async () => {
        try {
            const response = await axiosInstance.get('/api/banners');
            if (response.status === 200) {
                setBannerImage(BANNER_URL + response.data.data.bannerImageName);
                setBannerDescription(response.data.data.bannerDescription);
            }
        } catch (error) {
            console.error('배너 정보를 불러오는 중 오류가 발생했습니다.', error);
        }
    };

    const createBannerImage = async (e) => {
        const file = e.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onloadend = () => {
                setSelectedBanner(reader.result);
            };
            reader.readAsDataURL(file);

            // FormData에 직접 file 변수를 추가합니다.
            const formData = new FormData();
            formData.append('bannerFile', file);

            try {
                const response = await axiosInstance.post('/api/banners/image', formData, {
                    headers: { 'Content-Type': 'multipart/form-data' },
                });
                if (response.status === 200) {
                    fetchBanner();
                }
            } catch (error) {
                toast.error('배너 이미지 추가 실패');
            }
        }
    };

    const createBannerDescription = async () => {
        try {
            const response = await axiosInstance.post('/api/banners/description', {
                bannerDescription: bannerDescription
            });
            if (response.status === 200) {
                fetchBanner();
            }
        } catch (error) {
            toast.error('배너 설명 추가 실패');
        }
    };

    return (
        <div className={styles.manageBannerContainer}>
            <h2 className={styles.title}>
                <span>
                    배너 관리
                </span>
            </h2>
            <div>
                <div className={styles.bannerContainer}>
                    <img
                        src={selectedBanner || bannerImage}
                        alt="Banner"
                        className={styles.bannerImage}
                    />
                    <div
                        className={styles.bannerOverlay}
                        onClick={() => document.getElementById('banner-input').click()}
                    >
                        배너 수정
                    </div>
                    <input
                        id="banner-input"
                        type="file"
                        accept="image/*"
                        className={styles.hiddenInput}
                        onChange={createBannerImage}
                    />
                </div>

                <div className={styles.bannerInput}>
                    <div className={styles.inputGroup}>
                        <label>배너 설명</label>
                        <input
                            placeholder="배너 설명을 입력하세요"
                            value={bannerDescription}
                            onChange={(e) => setBannerDescription(e.target.value)}
                        />
                        <button type="button" onClick={createBannerDescription}>추가</button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ManageBanner;
