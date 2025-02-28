import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { useAuth } from '../../contexts/AuthContext';

const ProtectedRoute = ({ children }) => {
    const { authInfo, isLoading } = useAuth();
    const navigate = useNavigate();

    useEffect(() => {
        // 로딩이 완료되었고, authInfo가 null인 경우에만 리다이렉트 처리
        if (!isLoading && authInfo === null) {
            toast.error("로그아웃 되었습니다. 다시 로그인하여 이용해주세요");
            navigate("/");
        }
    }, [authInfo, isLoading, navigate]);

    // 인증 정보가 아직 준비되지 않았다면 (undefined) 또는 로딩 중이면 아무것도 렌더링하지 않음
    if (isLoading || authInfo === undefined) return null;

    // authInfo가 null이면 이미 useEffect에서 리다이렉트 처리됨
    return children;
};

export default ProtectedRoute;
