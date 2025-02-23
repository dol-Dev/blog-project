import React, { createContext, useContext, useEffect, useState } from 'react';
import axiosInstance from '../utils/axiosInstance';


// 1. Context 생성: 전역 상태 관리를 위한 Context 생성
const AuthContext = createContext();

// 2. export를 사용하면 다른 파일에서 import { AuthProvider } from '경로'로 사용 가능
export const AuthProvider = ({ children }) => {  // AuthProvider로 감싸는 jsx가 children으로 전달됨
    const [authInfo, setAuthInfo] = useState(null);
    const [loading, setLoading] = useState(true);

    // 사용자 정보를 불러오는 함수
    const fetchUserInfo = async () => {
        try {
            const response = await axiosInstance.get('/api/auth/info');
            if (response.status === 200 && response.data.data) {
                setAuthInfo(response.data.data);
            }
        } catch (error) {
            console.error('사용자 정보를 불러오는데 실패했습니다.', error);
            setAuthInfo(null);
        }
        setLoading(false);
    };

    useEffect(() => {
        fetchUserInfo();
    }, []);


    // 3. AuthContext.Provider를 통해 전역 상태 값(value)을 하위 컴포넌트에 제공
    // 단, Provider로 감싸진 하위 컴포넌트는 useContext(AuthContext) 또는 useAuth 훅을 통해 전역 상태 값을 사용 가능
    return (
        <AuthContext.Provider value={{ authInfo, loading, fetchUserInfo }}>
            {children}
        </AuthContext.Provider>
    );
};

// 4. useContext를 이용해 AuthContext에 제공된 전역 상태 값을 가져오는 훅을 명시 (useAuth)
// 굳이 userAuth가 아니여도 useContext(AuthContext)로도 사용 가능
export const useAuth = () => {
    return useContext(AuthContext);
};
