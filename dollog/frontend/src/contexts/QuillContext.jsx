import React, { createContext, useContext } from 'react';

export const QuillContext = createContext();

export const QuillProvider = ({ children }) => {

    const modules = {
        toolbar: {
            container: "#custom-quill-toolbar",
        }
    };

    const formats = [
        'font', 'size',
        'bold', 'italic', 'underline', 'strike',
        'color', 'background',
        'script',
        'header', 'blockquote', 'code-block',
        'indent', 'list',
        'direction', 'align',
        'link', 'image', 'video', 'formula'
    ];
    return (
        <QuillContext.Provider value={{ modules, formats }}>
            <div>
                {children}
            </div>
        </QuillContext.Provider>
    );
};

export const useQuill = () => useContext(QuillContext);
