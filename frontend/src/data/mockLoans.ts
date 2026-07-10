import type { Loan } from "../types/Loan";

export const mockLoans: Loan[] = [
    {
        id: 1,
        bookId: 1,
        userId: '1',
        borrowedAt: '2026-01-04',
        returnedAt: '2026-01-12',
        status: 'RETURNED'
    },
    {
        id: 2,
        bookId: 2,
        userId: '2',
        borrowedAt: '2026-02-04',
        returnedAt: null,
        status: 'BORROWED'
    }
]
