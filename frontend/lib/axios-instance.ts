import axios, { AxiosRequestConfig } from 'axios';


export const AXIOS_INSTANCE = axios.create({
    baseURL: process.env.NEXT_PUBLIC_API_URL,
});


AXIOS_INSTANCE.interceptors.request.use((config) => {
    const token = typeof window !== 'undefined' ? localStorage.getItem('token') : null;
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});


export const customInstance = <T>(
    config: AxiosRequestConfig,
    options?: AxiosRequestConfig,
): Promise<T> => {
    return AXIOS_INSTANCE({...config, ...options}).then(({ data }) => data);
};

export default customInstance;