import React, { createContext, useContext, useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';

const BlogContext = createContext();

export const BlogProvider = ({ children }) => {
    const location = useLocation();

    const [nickname, setNickname] = useState(sessionStorage.getItem("nickname") || "");
    const [provider, setProvider] = useState(sessionStorage.getItem("provider") || "");
    const [blogName, setBlogName] = useState(sessionStorage.getItem("blogName") || "");

    useEffect(() => {
        if (nickname) {
            sessionStorage.setItem("nickname", nickname);
        }
        if (provider) {
            sessionStorage.setItem("provider", provider);
        }
        if (blogName) {
            sessionStorage.setItem("blogName", blogName);
        }
    }, [nickname, provider]);

    useEffect(() => {

        if (location.pathname === "/posts") {
            return;
        }

        sessionStorage.removeItem("nickname");
        sessionStorage.removeItem("provider");
        sessionStorage.removeItem("blogName");
    }, [location.pathname]);


    return (
        <BlogContext.Provider value={{ nickname, provider, blogName, setNickname, setProvider, setBlogName }}>
            {children}
        </BlogContext.Provider>
    );
};

export const useBlog = () => useContext(BlogContext);