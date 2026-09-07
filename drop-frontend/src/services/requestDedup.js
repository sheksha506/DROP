const pendingRequests = new Map();

export const dedupedRequest = (
    key,
    requestFunction
) => {
    if (pendingRequests.has(key)) {
        return pendingRequests.get(key);
    }

    const request =
        Promise.resolve()
            .then(() => requestFunction())
            .finally(() => {
                pendingRequests.delete(key);
            });

    pendingRequests.set(
        key,
        request
    );

    return request;
};