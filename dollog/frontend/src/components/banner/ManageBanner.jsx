import React, { useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import { Button, Form, Segment } from 'semantic-ui-react';
import axiosInstance from '../../utils/axiosInstance'

const ManageBanner = () => {
    const [bannerImageUrl, setBannerImageUrl] = useState('');
    const [bannerDescription, setBannerDescription] = useState('');

    useEffect(() => {
        fetchBanner();
    }, []);

    const fetchBanner = async () => {
        try {
            const response = await axiosInstance.get('/api/banners');
            if (response.status === 200) {
                setBannerImageUrl(response.data.data.bannerImageUrl);
                setBannerDescription(response.data.data.bannerDescription);
            }
        } catch (error) {
            console.error('배너 정보를 불러오는 중 오류가 발생했습니다.', error);
        }
    };

    const handleSubmit = async () => {
        try {
            const response = await axiosInstance.post('/api/banners', {
                bannerImageUrl: bannerImageUrl,
                bannerDescription: bannerDescription,
            });
            if (response.status === 200) {
                fetchBanner();
            }
        } catch (error) {
            toast.error("배너 추가 실패")
        };
    };

    return (
        <div style={{ padding: '20px', paddingTop: '50px' }}>
            <h2 style={{
                display: 'flex', fontSize: '28px',
                fontWeight: 'bold'
            }}>배너 관리</h2>
            <Form style={{ width: '100%' }}>
                <Form.Field>
                    <label>배너 URL</label>
                    <input
                        placeholder='배너 이미지 URL을 입력하세요'
                        value={bannerImageUrl}
                        onChange={(e) => setBannerImageUrl(e.target.value)}
                    />
                </Form.Field>
                <Form.Field>
                    <label>배너 설명</label>
                    <input
                        placeholder='배너 설명을 입력하세요'
                        value={bannerDescription}
                        onChange={(e) => setBannerDescription(e.target.value)}
                    />
                </Form.Field>
                <Button
                    content="추가하기"
                    labelPosition='right'
                    icon='checkmark'
                    onClick={handleSubmit}
                    positive
                />
            </Form>
            <Segment>
                <h3>현재 배너</h3>
                <p><strong>URL:</strong> {bannerImageUrl}</p>
                <p><strong>설명:</strong> {bannerDescription}</p>
                <img src={bannerImageUrl} alt="Current Banner" style={{ maxWidth: '100%' }} />
            </Segment>
        </div>
    );
};

export default ManageBanner;
