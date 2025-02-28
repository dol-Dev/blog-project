// ToolbarOptions.js
const toolbarOptions = [
    [
        { className: "ql-font", options: ['serif', 'monospace'] },
        { className: "ql-size", options: ["small", "large", "huge"] }
    ],
    [
        { className: "ql-bold" },
        { className: "ql-italic" },
        { className: "ql-underline" },
        { className: "ql-strike" }
    ],
    [
        { className: "ql-color", options: ["red", "green", "blue", "orange", "violet"] },
        { className: "ql-background", options: ["red", "green", "blue", "orange", "violet"] }
    ],
    [
        { className: "ql-script", value: "sub" },
        { className: "ql-script", value: "super" }
    ],
    [
        { className: "ql-header", value: "1" },
        { className: "ql-header", value: "2" },
        { className: "ql-blockquote" },
        { className: "ql-code-block" }
    ],
    [
        { className: "ql-list", value: "ordered" },
        { className: "ql-list", value: "bullet" },
        { className: "ql-indent", value: "-1" },
        { className: "ql-indent", value: "+1" }
    ],
    [
        { className: "ql-direction", value: "rtl" },
        { className: "ql-align", options: ['right', 'center', 'justify'] }
    ],
    [
        { className: "ql-link" },
        { className: "ql-image" },
        { className: "ql-video" },
        { className: "ql-formula" }
    ]
];

export default toolbarOptions;
