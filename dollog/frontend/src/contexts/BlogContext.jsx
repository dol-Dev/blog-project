import React, { createContext, useContext, useEffect, useState } from 'react';

const BlogContext = createContext();

export const BlogProvider = ({ children }) => {
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

    return (
        <BlogContext.Provider value={{ nickname, provider, blogName, setNickname, setProvider, setBlogName }}>
            {children}
        </BlogContext.Provider>
    );
};

export const useBlog = () => useContext(BlogContext);