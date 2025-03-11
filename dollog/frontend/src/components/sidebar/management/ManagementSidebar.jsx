import React from 'react';
import { Link } from 'react-router-dom';
import styles from './managementSidebar.module.css';

const ManagementSidebar = () => {
    return (
        <div className={styles['management-sidebar']}>
            <Link to="/manage/dashboard" className={styles['management-sidebar-header']}>
                블로그 관리 홈
            </Link>
            <ul>
                <li>
                    <Link to="/manage/categorys">
                        카테고리 관리
                    </Link>
                </li>
                <li>
                    <Link to="/manage/posts" className={styles['management-sidebar-link']}>
                        게시글 관리
                    </Link>
                </li>
                <li>
                    <Link to="/manage/comments" className={styles['management-sidebar-link']}>
                        댓글 관리
                    </Link>
                </li>
                <li>
                    <Link to="/manage/banners" className={styles['management-sidebar-link']}>
                        배너 관리
                    </Link>
                </li>
            </ul>
        </div>
    );
};

export default ManagementSidebar;
