import React from 'react';
import styles from './managementDashboard.module.css'; // CSS 모듈을 임포트

function ManagementDashboard() {
    return (
        <div className={styles['management-dashboard-container']}>
            <h2 className={styles['management-dashboard-header']}>블로그 관리 홈</h2>
            <p className={styles['management-dashboard-description']}>
                블로그 관리 홈에 오신 것을 환영합니다. 여기에서 블로그의 다양한 설정과 관리를 할 수 있습니다.
            </p>
        </div>
    );
}

export default ManagementDashboard;
