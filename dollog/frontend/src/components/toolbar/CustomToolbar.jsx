// CustomToolbar.jsx
import React from 'react';
import './customToolbar.module.css';

const CustomToolbar = () => (
    <div id="custom-quill-toolbar">
        <span className="ql-formats">
            <select className="ql-header" defaultValue="">
                <option value="1">Header 1</option>
                <option value="2">Header 2</option>
            </select>

            <select className="ql-size" defaultValue="">
                <option value="small">Small</option>
                <option value="">Medium</option>
                <option value="large">Large</option>
                <option value="huge">Huge</option>
            </select>
        </span>

        <span className="ql-formats">
            <button className="ql-bold" />
            <button className="ql-italic" />
            <button className="ql-underline" />
            <button className="ql-strike" />
        </span>

        <span className="ql-formats">
            <select className="ql-color">
                <option selected></option>
                <option value="red"></option>
                <option value="green"></option>
                <option value="blue"></option>
                <option value="orange"></option>
                <option value="violet"></option>
            </select>

            <select className="ql-background">
                <option selected></option>
                <option value="red"></option>
                <option value="green"></option>
                <option value="blue"></option>
                <option value="orange"></option>
                <option value="violet"></option>
            </select>
        </span>

        <span className="ql-formats">
            <button className="ql-script" value="sub" />
            <button className="ql-script" value="super" />
        </span>

        <span className="ql-formats">
            <button className="ql-blockquote" />
        </span>

        <span className="ql-formats">
            <button className="ql-list" value="ordered" />
            <button className="ql-list" value="bullet" />
            <button className="ql-indent" value="-1" />
            <button className="ql-indent" value="+1" />
        </span>

        <span className="ql-formats">
            <button className="ql-direction" value="rtl" />
            <select className="ql-align">
                <option selected></option>
                <option value="center"></option>
                <option value="right"></option>
                <option value="justify"></option>
            </select>
        </span>

        <span className="ql-formats">
            <button className="ql-link" />
            <button className="ql-image" />
            <button className="ql-video" />
            <button className="ql-formula" />
        </span>

        <span className="ql-formats">
            <button className="ql-clean" />
        </span>
    </div>
);

export default CustomToolbar;
