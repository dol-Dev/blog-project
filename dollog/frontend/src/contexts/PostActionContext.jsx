import React, { createContext, useContext, useState } from 'react';

const PostActionContext = createContext({
    onSubmit: null,
    setOnSubmit: () => { },
});

export const usePostAction = () => useContext(PostActionContext);

export const PostActionProvider = ({ children }) => {
    const [onSubmit, setOnSubmit] = useState(null);

    return (
        <PostActionContext.Provider value={{ onSubmit, setOnSubmit }}>
            {children}
        </PostActionContext.Provider>
    );
};
