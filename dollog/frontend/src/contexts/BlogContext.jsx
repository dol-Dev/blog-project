import React, { createContext, useContext, useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';

const BlogContext = createContext();

export const BlogProvider = ({ children }) => {
    const location = useLocation();

    const [nickname, setNickname] = useState(sessionStorage.getItem("nickname") || "");
    const [provider, setProvider] = useState(sessionStorage.getItem("provider") || "");
    const [blogName, setBlogName] = useState(sessionStorage.getItem("blogName") || "");

    // 상태를 sessionStorage에 저장 또는 삭제
    useEffect(() => {
        if (nickname) {
            sessionStorage.setItem("nickname", nickname);
        } else {
            sessionStorage.removeItem("nickname");
        }
        if (provider) {
            sessionStorage.setItem("provider", provider);
        } else {
            sessionStorage.removeItem("provider");
        }
        if (blogName) {
            sessionStorage.setItem("blogName", blogName);
        } else {
            sessionStorage.removeItem("blogName");
        }
    }, [nickname, provider, blogName]);

    useEffect(() => {
        const allowedPaths = ['/posts'];
        // 현재 경로가 allowedPaths 중 하나로 시작하지 않으면 초기화
        if (!allowedPaths.some(path => location.pathname.startsWith(path))) {
            setNickname("");
            setProvider("");
            setBlogName("");
        }
    }, [location.pathname]);

    return (
        <BlogContext.Provider value={{ nickname, provider, blogName, setNickname, setProvider, setBlogName }}>
            {children}
        </BlogContext.Provider>
    );
};

export const useBlog = () => useContext(BlogContext);