import React from 'react';
import { Button } from 'semantic-ui-react';
import { usePostAction } from '../../contexts/PostActionContext';
import styles from './postFooter.module.css';

const PostFooter = () => {
    const { onSubmit } = usePostAction();

    return (
        <footer className={styles.footer}>
            <Button type="button" onClick={onSubmit} disabled={!onSubmit}>
                {onSubmit ? '게시글 작성' : '함수 없음'}
            </Button>
            <div>
                <p>© {new Date().getFullYear()} dol_dev. All rights reserved.</p>
            </div>
        </footer>
    );
};

export default PostFooter;