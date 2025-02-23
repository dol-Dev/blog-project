import React, { createContext, useContext, useState } from 'react';

const BlogContext = createContext();

export const BlogProvider = ({ children }) => {
    const [blogName, setBlogName] = useState('');
    const [nickname, setNickname] = useState('');

    return (
        <BlogContext.Provider value={{ blogName, setBlogName, nickname, setNickname }}>
            {children}
        </BlogContext.Provider>
    );
};


export const useBlog = () => useContext(BlogContext);