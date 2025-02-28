// CustomToolbar.jsx
import React from "react";
import toolbarOptions from './ToolbarOptions'; // formats 배열을 export한 파일
import styles from './customToolbar.module.css';

const renderOptions = (formatData) => {
    const { className, options } = formatData;
    return (
        <select className={className}>
            <option defaultValue=""></option>
            {options.map(value => (
                <option key={value} value={value}>{value}</option>
            ))}
        </select>
    );
};

const renderSingle = (formatData) => {
    const { className, value } = formatData;
    return (
        <button className={className} value={value} />
    );
};

// id="custom-quill-toolbar"인 컨테이너 생성 -> QuillContext에서 modules를 DOM에서 가져옴 -> WhitePost, UpdatePost로 modules를 전달
const CustomToolbar = () => (
    <div id="custom-quill-toolbar">
        {toolbarOptions.map((group, idx) => (
            <span key={idx} className="ql-formats">
                {group.map(formatData =>
                    formatData.options ? renderOptions(formatData) : renderSingle(formatData)
                )}
            </span>
        ))}
    </div>
);

export default CustomToolbar;
