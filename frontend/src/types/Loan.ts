export type LoanStatus = 'BORROWED' | 'RETURNED'


export type Loan = {
    id : string;
    bookId : string;
    userId : string;
    borrowedAt : string;
    returnedAt : string | null;
    status : LoanStatus;
}
