import React from 'react';
import { Outlet } from 'react-router-dom';
import styles from './managementLayout.module.css';
import ManagementSidebar from '../../../components/sidebar/management/ManagementSidebar';

const ManagementLayout = () => {
    return (
        <div className={styles['management-app']}>
            <ManagementSidebar />
            <main className={styles['management-content']}>
                <Outlet />
            </main>
        </div>
    );
};

export default ManagementLayout;
