export interface LoginType{
    email: string,
    password: string,
}

export interface RegisterType{
    name: string,
    lastName: string,
    email: string,
    password: string,
    confirmPass?: string
}

