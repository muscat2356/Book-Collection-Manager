export type LoanStatus = 'BORROWED' | 'RETURNED'


export type Loan = {
    id : number;
    bookId : number;
    userId : string;
    borrowedAt : string;
    returnedAt : string | null;
    status : LoanStatus;
}
