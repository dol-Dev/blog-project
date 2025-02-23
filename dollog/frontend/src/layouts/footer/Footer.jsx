import React from 'react';
import 'bootstrap/dist/css/bootstrap.css';
import 'react-bootstrap';
import styles from './footer.module.css';

const Footer = () => {
    return (
        <footer className={styles.footer}>
            <div>
                <p>© {new Date().getFullYear()} dol_dev. All rights reserved.</p>
                <nav>
                    <a href="/about">About</a>
                    <a href="/contact">Contact</a>
                    <a href="/privacy">Privacy Policy</a>
                </nav>
            </div>
        </footer>
    );
};

export default Footer;
