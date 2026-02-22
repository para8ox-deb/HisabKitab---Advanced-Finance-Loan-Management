import { useState } from 'react';
import api from '../../api/axios';
import toast from 'react-hot-toast';
import {
    HiOutlineDocumentDownload,
    HiOutlineTable,
    HiOutlineChartBar,
} from 'react-icons/hi';

/**
 * Reports — A page for lenders to download data exports.
 */
const Reports = () => {
    const [loading, setLoading] = useState({
        loans: false,
        summary: false
    });

    const downloadReport = async (type, filename) => {
        setLoading(prev => ({ ...prev, [type]: true }));
        try {
            // For CSV downloads, we need 'blob' response type
            const response = await api.get(`/reports/${type}`, {
                responseType: 'blob',
            });

            // Create a temporary URL for the blob
            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', filename);
            document.body.appendChild(link);
            link.click();

            // Cleanup
            link.remove();
            window.URL.revokeObjectURL(url);
            toast.success(`${filename} downloaded!`);
        } catch (error) {
            toast.error('Failed to download report');
            console.error(error);
        } finally {
            setLoading(prev => ({ ...prev, [type]: false }));
        }
    };

    return (
        <div className="fade-in">
            <div className="page-header">
                <div>
                    <h1 className="page-title">Reports & Exports</h1>
                    <p className="page-subtitle">Download your lending data in CSV format for Excel or bookkeeping</p>
                </div>
            </div>

            <div className="dashboard-grid">
                {/* All Loans Report */}
                <div className="card">
                    <div style={{ display: 'flex', gap: '20px', alignItems: 'flex-start' }}>
                        <div className="stat-icon" style={{ background: 'var(--primary-50)', color: 'var(--primary-600)', fontSize: '2rem', width: '60px', height: '60px' }}>
                            <HiOutlineTable />
                        </div>
                        <div style={{ flex: 1 }}>
                            <h3 className="card-title">All Loans Report</h3>
                            <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', margin: '8px 0 20px 0' }}>
                                A full list of every loan you've ever issued, including principal, interest rate, status, and outstanding balance.
                            </p>
                            <button
                                className="btn btn-primary"
                                onClick={() => downloadReport('loans', 'all_loans_report.csv')}
                                disabled={loading.loans}
                            >
                                {loading.loans ? <div className="spinner spinner-sm"></div> : <><HiOutlineDocumentDownload /> Download CSV</>}
                            </button>
                        </div>
                    </div>
                </div>

                {/* Financial Summary */}
                <div className="card">
                    <div style={{ display: 'flex', gap: '20px', alignItems: 'flex-start' }}>
                        <div className="stat-icon" style={{ background: 'var(--success-50)', color: 'var(--success-600)', fontSize: '2rem', width: '60px', height: '60px' }}>
                            <HiOutlineChartBar />
                        </div>
                        <div style={{ flex: 1 }}>
                            <h3 className="card-title">Financial Summary</h3>
                            <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', margin: '8px 0 20px 0' }}>
                                A high-level summary of your portfolio performance: Total lent, Total interest earned, and Recovery rate.
                            </p>
                            <button
                                className="btn btn-success"
                                onClick={() => downloadReport('summary', 'financial_summary.csv')}
                                disabled={loading.summary}
                            >
                                {loading.summary ? <div className="spinner spinner-sm"></div> : <><HiOutlineDocumentDownload /> Download CSV</>}
                            </button>
                        </div>
                    </div>
                </div>

                {/* Note about Individual Loan Reports */}
                <div className="card full-width" style={{ borderStyle: 'dashed', background: 'var(--gray-50)' }}>
                    <div style={{ display: 'flex', items: 'center', gap: '12px' }}>
                        <span style={{ fontSize: '1.5rem' }}>💡</span>
                        <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
                            <strong>Tip:</strong> Need the interest schedule for a specific loan? Go to the <strong>Loans</strong> list, click <strong>View Details</strong>, and use the <strong>Print Schedule</strong> button to save it as a PDF!
                        </p>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Reports;
