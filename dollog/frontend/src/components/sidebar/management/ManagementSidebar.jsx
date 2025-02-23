import React from 'react';
import { Link } from 'react-router-dom';
import styles from './managementSidebar.module.css';

const ManagementSidebar = () => {
    return (
        <div className={styles['management-sidebar']}>
            <div className={styles['management-sidebar-header']}>
                블로그 관리 홈
            </div>
            <ul>
                <li>
                    <Link to="/manage/categorys" className={styles['management-sidebar-link']}>
                        카테고리 관리
                    </Link>
                </li>
            </ul>
        </div>
    );
};

export default ManagementSidebar;
