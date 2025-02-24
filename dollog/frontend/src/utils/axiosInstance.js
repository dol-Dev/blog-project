import axios from 'axios';

const axiosInstance = axios.create({
    baseURL: 'http://localhost:8080',
    withCredentials: true, // 쿠키를 자동으로 전송하도록 설정
});

export default axiosInstance;