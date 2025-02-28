import React, { createContext, useContext, useState } from 'react';
import styles from './quillProvider.module.css';

export const QuillContext = createContext();

export const QuillProvider = ({ children }) => {
    const [quillInstance, setQuillInstance] = useState(null);

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
        <QuillContext.Provider value={{ quillInstance, setQuillInstance, modules, formats }}>
            <div className={styles.providerWrapper}>
                {children}
            </div>
        </QuillContext.Provider>
    );
};

export const useQuill = () => useContext(QuillContext);
