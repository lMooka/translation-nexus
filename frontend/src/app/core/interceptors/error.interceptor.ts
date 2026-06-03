import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      let userFriendlyMessage = '';

      if (err.error && typeof err.error === 'object') {
        const code = err.error.code;
        let msg = err.error.message || '';

        // Strip "code - " prefix
        if (code && msg.startsWith(code + ' - ')) {
          msg = msg.substring(code.length + 3);
        }

        // Strip " - uniqueId:[...]" suffix
        const uniqueIdIdx = msg.indexOf(' - uniqueId:[');
        if (uniqueIdIdx !== -1) {
          msg = msg.substring(0, uniqueIdIdx);
        }

        userFriendlyMessage = msg || code || 'An unexpected error occurred.';
      } else if (typeof err.error === 'string') {
        userFriendlyMessage = err.error;
      } else {
        userFriendlyMessage = err.message || 'An unexpected error occurred.';
      }

      // Return a new HttpErrorResponse with the parsed message as the error payload
      const modifiedError = new HttpErrorResponse({
        error: userFriendlyMessage,
        headers: err.headers,
        status: err.status,
        statusText: err.statusText,
        url: err.url || undefined
      });

      return throwError(() => modifiedError);
    })
  );
};
