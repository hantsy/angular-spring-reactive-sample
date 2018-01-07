/* tslint:disable:no-unused-variable */

import { TestBed, async } from '@angular/core/testing';
import { Nl2brPipe } from './nl2br.pipe';

describe('Pipe: Nl2br', () => {
  it('create an instance', () => {
    let pipe = new Nl2brPipe();
    expect(pipe).toBeTruthy();

    expect(pipe.transform('hello\nworld')).toEqual('hello<br>world');
  });
});
